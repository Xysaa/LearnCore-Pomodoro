package com.example.pomodoro.ble

import com.example.pomodoro.domain.model.EspMessage
import com.example.pomodoro.domain.model.IoTState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** iOS stub — BLE iOS memerlukan CoreBluetooth via Swift interop */
class BleServiceIosStub : BleService {
    private val _iotState   = MutableStateFlow(IoTState())
    private val _espMessage = MutableSharedFlow<EspMessage>(replay = 1, extraBufferCapacity = 16)
    override val iotState: StateFlow<IoTState>   = _iotState
    override val espMessage: SharedFlow<EspMessage> = _espMessage

    override fun startScan(deviceName: String) {}
    override fun stopScan() {}
    override fun connectGatt(deviceAddress: String) {}
    override fun disconnect() {}
    override fun release() {}
    override fun sendCommand(json: String) {}
}

actual fun createBleService(): BleService = BleServiceIosStub()
