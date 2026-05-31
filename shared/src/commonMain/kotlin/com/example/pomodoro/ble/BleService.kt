package com.example.pomodoro.ble

import com.example.pomodoro.domain.model.EspMessage
import com.example.pomodoro.domain.model.IoTState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface BLE platform-agnostic.
 * - Android : BleServiceAndroid (BluetoothLeScanner + BluetoothGatt)
 * - iOS     : BleServiceIosStub (no-op)
 *
 * Data flow:
 *   ESP32 ──BLE Notify──▶ espMessage (parsed EspMessage)
 *                       ──────────▶ iotState (diupdate otomatis dari espMessage)
 *
 * Command flow:
 *   App ──sendCommand(json)──▶ ESP32 via BLE Write
 */
interface BleService {
    val iotState: StateFlow<IoTState>
    /** SharedFlow — setiap notifikasi dari ESP32 selalu di-emit, termasuk duplikat */
    val espMessage: SharedFlow<EspMessage>

    fun startScan(deviceName: String)
    fun stopScan()
    fun connectGatt(deviceAddress: String)
    fun disconnect()
    fun release()

    /** Kirim perintah JSON ke ESP32 via BLE Write characteristic */
    fun sendCommand(json: String)
}

expect fun createBleService(): BleService
