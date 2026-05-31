package com.example.pomodoro.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.pomodoro.domain.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

// ── BLE UUIDs — harus sama persis dengan firmware ESP32 ────
private val SVC_UUID     = UUID.fromString("abcdef00-1234-5678-1234-56789abcdef0")
private val TX_CHAR_UUID = UUID.fromString("abcdef01-1234-5678-1234-56789abcdef0") // NOTIFY
private val RX_CHAR_UUID = UUID.fromString("abcdef02-1234-5678-1234-56789abcdef0") // WRITE
private val CCCD_UUID    = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

private const val SCAN_TIMEOUT_MS = 15_000L
private const val RECONNECT_DELAY = 3_000L
private const val DISCOVER_DELAY  = 600L    // delay sebelum discoverServices

@SuppressLint("MissingPermission")
class BleServiceAndroid(private val context: Context) : BleService {

    private val btManager  = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter    = btManager?.adapter
    private val mainThread = Handler(Looper.getMainLooper())

    private var scanner      : BluetoothLeScanner?              = null
    private var gatt         : BluetoothGatt?                   = null
    private var txChar       : BluetoothGattCharacteristic?     = null
    private var rxChar       : BluetoothGattCharacteristic?     = null
    private var scanning     = false
    private var autoReconnect = true

    private val _iotState    = MutableStateFlow(IoTState())
    override val iotState    : StateFlow<IoTState>   = _iotState

    // SharedFlow: replay=1 → subscriber baru langsung dapat state terakhir
    // extraBufferCapacity=32 → BLE callback thread tidak drop event
    private val _espMessage  = MutableSharedFlow<EspMessage>(replay = 1, extraBufferCapacity = 32)
    override val espMessage  : SharedFlow<EspMessage> = _espMessage

    private var targetName = "LEARNCORE-POMO"

    // ── MTU / fragmentation buffer ───────────────────────────
    // BLE default MTU = 23 bytes (payload 20 bytes). JSON ESP32 bisa >20 bytes,
    // sehingga dikirim dalam beberapa packet terpisah. Buffer ini mengumpulkan
    // fragment sampai JSON lengkap (ditandai dengan '}' penutup yang valid).
    private val rxBuffer = StringBuilder()

    // ────────────────────────────────────────────────────────
    //  SCAN CALLBACK
    // ────────────────────────────────────────────────────────
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Filter manual — lebih reliable dari ScanFilter.setDeviceName()
            // karena beberapa HP tidak mengisi field name di advertising packet
            val name = result.device.name ?: result.scanRecord?.deviceName ?: ""
            if (!name.equals(targetName, ignoreCase = true)) return

            log("Ditemukan: $name [${result.device.address}] rssi=${result.rssi}")
            stopScan()
            setState {
                copy(
                    bleStatus     = BleConnectionStatus.CONNECTING,
                    deviceName    = name,
                    deviceAddress = result.device.address,
                    debugLog      = "Menemukan $name, menghubungkan..."
                )
            }
            connectGatt(result.device.address)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            val msg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED           -> "Scan sudah berjalan"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Registrasi gagal"
                SCAN_FAILED_FEATURE_UNSUPPORTED       -> "BLE tidak didukung"
                SCAN_FAILED_INTERNAL_ERROR            -> "Error internal BLE"
                else                                  -> "Error scan: $errorCode"
            }
            log("Scan gagal: $msg")
            setState { copy(bleStatus = BleConnectionStatus.ERROR, debugLog = "Scan gagal: $msg") }
        }
    }

    // ────────────────────────────────────────────────────────
    //  GATT CALLBACK
    // ────────────────────────────────────────────────────────
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            log("onConnectionStateChange status=$status newState=$newState")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                // Koneksi gagal (misal status 133 = GATT_ERROR / timeout)
                log("GATT error status=$status — close & reconnect")
                gatt?.close()
                gatt   = null
                txChar = null
                rxChar = null
                setState {
                    copy(
                        bleStatus = BleConnectionStatus.DISCONNECTED,
                        debugLog  = "GATT error ($status), mencoba ulang..."
                    )
                }
                if (autoReconnect) scheduleReconnect()
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    log("GATT terhubung → discoverServices dalam ${DISCOVER_DELAY}ms")
                    setState {
                        copy(
                            bleStatus     = BleConnectionStatus.CONNECTED,
                            deviceAddress = g.device.address,
                            deviceName    = g.device.name ?: targetName,
                            debugLog      = "Terhubung, mendeteksi layanan..."
                        )
                    }
                    mainThread.postDelayed({ g.discoverServices() }, DISCOVER_DELAY)
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    log("GATT terputus")
                    gatt?.close()
                    gatt   = null
                    txChar = null
                    rxChar = null
                    rxBuffer.clear()
                    setState {
                        copy(
                            bleStatus         = BleConnectionStatus.DISCONNECTED,
                            presenceStatus    = PresenceStatus.UNKNOWN,
                            espRunning        = false,
                            espPaused         = false,
                            espPausedBySensor = false,
                            espPhase          = EspPhase.IDLE,
                            debugLog          = "Terputus"
                        )
                    }
                    if (autoReconnect) scheduleReconnect()
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            log("onServicesDiscovered status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                setState { copy(bleStatus = BleConnectionStatus.ERROR, debugLog = "Gagal mendeteksi layanan ($status)") }
                return
            }

            // Cetak semua service untuk debug
            g.services.forEach { svc ->
                log("  Service: ${svc.uuid}")
                svc.characteristics.forEach { c -> log("    Char: ${c.uuid} props=${c.properties}") }
            }

            val svc = g.getService(SVC_UUID)
            if (svc == null) {
                log("Service $SVC_UUID TIDAK ditemukan!")
                setState {
                    copy(
                        bleStatus = BleConnectionStatus.ERROR,
                        debugLog  = "Service UUID tidak ditemukan. Cek firmware."
                    )
                }
                return
            }

            txChar = svc.getCharacteristic(TX_CHAR_UUID)
            rxChar = svc.getCharacteristic(RX_CHAR_UUID)
            log("txChar=$txChar rxChar=$rxChar")

            if (txChar == null) {
                setState { copy(debugLog = "TX char tidak ditemukan!") }
                return
            }

            setState { copy(debugLog = "Mengaktifkan notifikasi...") }
            // Request MTU 512 agar JSON panjang tidak terpotong.
            // enableNotify dipanggil di onMtuChanged setelah MTU berhasil.
            val mtuOk = g.requestMtu(512)
            log("requestMtu(512) = $mtuOk")
            if (!mtuOk) {
                // requestMtu tidak didukung — langsung enable notify dengan MTU default
                enableNotify(g, txChar!!)
            }
        }

        // MTU negotiation — minta 512 bytes agar JSON tidak terpotong
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            log("onMtuChanged mtu=$mtu status=$status")
            setState { copy(debugLog = "MTU=$mtu — mengaktifkan notifikasi...") }
            // Setelah MTU berhasil dinegosiasi, baru aktifkan notify
            val tx = txChar ?: return
            enableNotify(gatt, tx)
        }

        // API 33+
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == TX_CHAR_UUID) {
                val raw = value.toString(Charsets.UTF_8).trim()
                log("RX: $raw")
                handlePayload(raw)
            }
        }

        // API < 33
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == TX_CHAR_UUID) {
                val raw = characteristic.value?.toString(Charsets.UTF_8)?.trim() ?: return
                log("RX (legacy): $raw")
                handlePayload(raw)
            }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            log("onDescriptorWrite status=$status uuid=${descriptor.uuid}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setState { copy(debugLog = "Notifikasi aktif — menunggu data...") }
            } else {
                setState { copy(debugLog = "Gagal aktifkan notifikasi ($status)") }
            }
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            log("onCharacteristicWrite status=$status")
        }
    }

    // ────────────────────────────────────────────────────────
    //  PUBLIC API
    // ────────────────────────────────────────────────────────
    override fun startScan(deviceName: String) {
        targetName    = deviceName
        autoReconnect = true

        if (adapter == null) {
            setState { copy(bleStatus = BleConnectionStatus.ERROR, debugLog = "Bluetooth tidak tersedia") }
            return
        }
        if (!adapter.isEnabled) {
            setState { copy(bleStatus = BleConnectionStatus.ERROR, debugLog = "Bluetooth mati — nyalakan Bluetooth") }
            return
        }
        if (_iotState.value.bleStatus == BleConnectionStatus.CONNECTED) {
            log("Sudah terhubung, skip scan")
            return
        }
        if (scanning) {
            log("Sudah scanning, skip")
            return
        }

        scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            setState { copy(bleStatus = BleConnectionStatus.ERROR, debugLog = "BluetoothLeScanner null") }
            return
        }

        log("Mulai scan untuk '$deviceName'...")
        setState { copy(bleStatus = BleConnectionStatus.SCANNING, debugLog = "Mencari '$deviceName'...") }
        scanning = true

        // TIDAK pakai ScanFilter.setDeviceName() — filter manual di callback
        // karena beberapa HP Android gagal match filter nama ESP32
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(null, settings, scanCallback)   // null = tanpa filter

        // Timeout scan
        mainThread.postDelayed({
            if (scanning) {
                log("Scan timeout setelah ${SCAN_TIMEOUT_MS}ms")
                stopScan()
                if (_iotState.value.bleStatus == BleConnectionStatus.SCANNING) {
                    setState {
                        copy(
                            bleStatus = BleConnectionStatus.DISCONNECTED,
                            debugLog  = "Tidak ditemukan. Pastikan ESP32 nyala & BLE aktif."
                        )
                    }
                }
            }
        }, SCAN_TIMEOUT_MS)
    }

    override fun stopScan() {
        if (scanning) {
            try { scanner?.stopScan(scanCallback) } catch (_: Exception) {}
            scanning = false
            log("Scan dihentikan")
        }
    }

    override fun connectGatt(deviceAddress: String) {
        val device = try { adapter?.getRemoteDevice(deviceAddress) }
                     catch (_: Exception) { null } ?: run {
            log("getRemoteDevice gagal untuk $deviceAddress")
            return
        }
        log("connectGatt → $deviceAddress")
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun disconnect() {
        log("disconnect() dipanggil")
        autoReconnect = false
        stopScan()
        mainThread.removeCallbacksAndMessages(null)
        gatt?.disconnect()
    }

    override fun release() {
        autoReconnect = false
        disconnect()
        gatt?.close()
        gatt = null
    }

    override fun sendCommand(json: String) {
        val char = rxChar ?: run { log("sendCommand: rxChar null"); return }
        val g    = gatt   ?: run { log("sendCommand: gatt null");   return }
        if (_iotState.value.bleStatus != BleConnectionStatus.CONNECTED) {
            log("sendCommand: tidak connected, skip")
            return
        }
        log("TX: $json")
        val bytes = json.toByteArray(Charsets.UTF_8)
        // Coba WRITE_TYPE_DEFAULT dulu (lebih kompatibel)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            g.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        } else {
            @Suppress("DEPRECATION")
            char.value = bytes
            @Suppress("DEPRECATION")
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            g.writeCharacteristic(char)
        }
    }

    // ────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ────────────────────────────────────────────────────────
    private fun enableNotify(g: BluetoothGatt, char: BluetoothGattCharacteristic) {
        val ok = g.setCharacteristicNotification(char, true)
        log("setCharacteristicNotification=$ok")

        val desc = char.getDescriptor(CCCD_UUID)
        if (desc == null) {
            log("CCCD descriptor tidak ditemukan pada ${char.uuid}")
            // Beberapa firmware tidak punya CCCD — coba tetap lanjut
            setState { copy(debugLog = "Terhubung (tanpa CCCD)") }
            return
        }

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val r = g.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            log("writeDescriptor API33 result=$r")
        } else {
            @Suppress("DEPRECATION")
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            val r = g.writeDescriptor(desc)
            log("writeDescriptor legacy result=$r")
        }
    }

    private fun scheduleReconnect() {
        log("Auto-reconnect dalam ${RECONNECT_DELAY}ms")
        mainThread.postDelayed({
            if (_iotState.value.bleStatus == BleConnectionStatus.DISCONNECTED && autoReconnect) {
                startScan(targetName)
            }
        }, RECONNECT_DELAY)
    }

    /**
     * Dipanggil setiap kali BLE notification datang.
     * BLE MTU default 23 bytes (payload 20 bytes), JSON ESP32 bisa lebih panjang.
     * Setelah requestMtu(512) berhasil, payload bisa sampai ~509 bytes sekaligus.
     * Tetap ada buffer untuk jaga-jaga kalau MTU tidak dinegosiasi.
     */
    private fun handlePayload(chunk: String) {
        rxBuffer.append(chunk)
        val buffered = rxBuffer.toString()

        // Cari JSON object lengkap: mulai '{' sampai '}' yang matching
        val start = buffered.indexOf('{')
        if (start < 0) { rxBuffer.clear(); return }

        // Hitung keseimbangan kurung kurawal untuk deteksi JSON lengkap
        var depth = 0
        var end   = -1
        for (i in start until buffered.length) {
            when (buffered[i]) {
                '{'  -> depth++
                '}'  -> { depth--; if (depth == 0) { end = i; break } }
            }
        }

        if (end < 0) {
            // JSON belum lengkap — tunggu chunk berikutnya
            log("Buffer menunggu: ${buffered.length} bytes")
            // Limit buffer 2048 char agar tidak leak memory
            if (rxBuffer.length > 2048) { rxBuffer.clear(); log("Buffer overflow, reset") }
            return
        }

        // JSON lengkap ditemukan
        val json = buffered.substring(start, end + 1)
        // Sisakan sisa sesudah JSON (bisa ada packet berikutnya)
        rxBuffer.clear()
        if (end + 1 < buffered.length) {
            rxBuffer.append(buffered.substring(end + 1))
        }

        log("JSON lengkap (${json.length}b): $json")
        processJson(json)
    }

    private fun processJson(raw: String) {
        val msg = parseEspMessage(raw)
        _espMessage.tryEmit(msg)

        val now = System.currentTimeMillis()
        when (msg) {
            is EspMessage.Status -> setState {
                copy(
                    adc               = msg.adc,
                    airBad            = msg.airBad,
                    distanceCm        = msg.distanceCm,
                    presenceStatus    = if (msg.userPresent) PresenceStatus.PRESENT else PresenceStatus.ABSENT,
                    espRunning        = msg.running,
                    espPaused         = msg.paused,
                    espPausedBySensor = msg.pausedBySensor,
                    espPhase          = msg.phase,
                    espSecsLeft       = msg.secsLeft,
                    espSess           = msg.sess,
                    espCycles         = msg.cycles,
                    espVolume         = msg.vol,
                    // dfOk default true — jika audio sudah terbukti jalan, anggap OK
                    // ESP32 kadang kirim dfOk:false saat init sebelum DFPlayer ready
                    dfOk              = msg.dfOk || _iotState.value.dfOk,
                    lastUpdated       = now,
                    rawJson           = raw,
                    debugLog          = "Status: ${msg.phase} run=${msg.running} secs=${msg.secsLeft}"
                )
            }
            is EspMessage.Air -> setState {
                copy(adc = msg.adc, airBad = msg.airBad, lastUpdated = now, rawJson = raw)
            }
            is EspMessage.User -> setState {
                copy(
                    distanceCm     = msg.distanceCm,
                    presenceStatus = if (msg.present) PresenceStatus.PRESENT else PresenceStatus.ABSENT,
                    lastUpdated    = now,
                    rawJson        = raw
                )
            }
            is EspMessage.Sensor -> setState {
                copy(
                    distanceCm     = msg.distanceCm,
                    presenceStatus = if (msg.userPresent) PresenceStatus.PRESENT else PresenceStatus.ABSENT,
                    adc            = msg.adc,
                    airBad         = msg.airBad,
                    lastUpdated    = now,
                    rawJson        = raw
                )
            }
            is EspMessage.Msg -> setState {
                copy(lastMsg = msg.text, lastUpdated = now, rawJson = raw)
            }
            is EspMessage.Unknown -> {
                log("Parse error untuk: $raw")
                setState { copy(rawJson = raw, debugLog = "Parse error: ${raw.take(60)}") }
            }
        }
    }

    private fun log(msg: String) {
        android.util.Log.d("BleService", msg)
    }

    private inline fun setState(block: IoTState.() -> IoTState) {
        _iotState.value = _iotState.value.block()
    }
}
