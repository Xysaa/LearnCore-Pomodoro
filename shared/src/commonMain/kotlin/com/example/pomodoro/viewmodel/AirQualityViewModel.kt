package com.example.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.ble.BleService
import com.example.pomodoro.domain.model.AirQualityData
import com.example.pomodoro.domain.model.EspMessage
import com.example.pomodoro.repository.currentTimeMillis
import kotlinx.coroutines.flow.*

data class AirQualityUiState(
    val current: AirQualityData = AirQualityData(),
    val history: List<AirQualityData> = emptyList(),
    val isLive: Boolean = false
)

class AirQualityViewModel(
    private val bleService: BleService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AirQualityUiState())
    val uiState: StateFlow<AirQualityUiState> = _uiState.asStateFlow()

    private val history = mutableListOf<AirQualityData>()

    init {
        // Collect dari SharedFlow — setiap event selalu diterima
        bleService.espMessage
            .onEach { msg ->
                val adc = when (msg) {
                    is EspMessage.Air    -> msg.adc
                    is EspMessage.Status -> msg.adc
                    is EspMessage.Sensor -> msg.adc
                    else                 -> return@onEach
                }
                val aqData = AirQualityData(adc = adc, timestamp = currentTimeMillis())
                history.add(aqData)
                if (history.size > 48) history.removeAt(0)

                _uiState.update { current ->
                    current.copy(
                        current = aqData,
                        history = history.toList(),
                        isLive  = bleService.iotState.value.isConnected
                    )
                }
            }
            .launchIn(viewModelScope)

        // Update isLive saat status koneksi berubah
        bleService.iotState
            .onEach { iot ->
                _uiState.update { it.copy(isLive = iot.isConnected) }
            }
            .launchIn(viewModelScope)
    }
}
