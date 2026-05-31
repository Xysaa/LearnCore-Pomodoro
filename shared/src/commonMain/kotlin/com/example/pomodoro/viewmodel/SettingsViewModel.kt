package com.example.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.domain.model.AppSettings
import com.example.pomodoro.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SessionRepository,
    private val pomodoroViewModel: PomodoroViewModel
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val bleConnectionStatus = pomodoroViewModel.iotState
        .map { it.bleStatus }
        .stateIn(viewModelScope, SharingStarted.Eagerly, pomodoroViewModel.iotState.value.bleStatus)

    init {
        repository.settings
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }

    fun updateFocusDuration(minutes: Int)    = save(_settings.value.copy(focusDurationMinutes  = minutes.coerceIn(1, 60)))
    fun updateShortBreak(minutes: Int)        = save(_settings.value.copy(shortBreakMinutes     = minutes.coerceIn(1, 15)))
    fun updateLongBreak(minutes: Int)         = save(_settings.value.copy(longBreakMinutes      = minutes.coerceIn(5, 60)))
    fun updateSessionsBeforeLong(n: Int)      = save(_settings.value.copy(sessionsBeforeLongBreak = n.coerceIn(2, 8)))
    fun updateVolume(v: Int)                  = save(_settings.value.copy(volume               = v.coerceIn(0, 30)))
    fun updateDeviceName(name: String)        = save(_settings.value.copy(bleDeviceName        = name))
    fun toggleNotifySession(on: Boolean)      = save(_settings.value.copy(notifySessionDone    = on))
    fun toggleNotifyAir(on: Boolean)          = save(_settings.value.copy(notifyAirQuality     = on))
    fun toggleNotifyAutoDetect(on: Boolean)   = save(_settings.value.copy(notifyAutoDetect     = on))
    fun toggleSound(on: Boolean)              = save(_settings.value.copy(soundEnabled         = on))
    fun toggleReducedMotion(on: Boolean)      = save(_settings.value.copy(reducedMotion        = on))
    fun toggleColorBlind(on: Boolean)         = save(_settings.value.copy(colorBlindMode       = on))

    fun connectBle()    = pomodoroViewModel.startBleConnection()
    fun disconnectBle() = pomodoroViewModel.stopBleConnection()

    /** Simpan settings lokal & sinkron ke ESP32 */
    private fun save(updated: AppSettings) {
        viewModelScope.launch {
            pomodoroViewModel.updateSettings(updated)
        }
    }

    /** Push config ke ESP32 tanpa start timer */
    fun pushConfigToEsp() = pomodoroViewModel.syncConfigToEsp()
}
