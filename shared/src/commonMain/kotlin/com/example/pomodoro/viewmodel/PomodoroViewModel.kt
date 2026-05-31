package com.example.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.ble.BleService
import com.example.pomodoro.domain.model.*
import com.example.pomodoro.repository.SessionRepository
import com.example.pomodoro.repository.currentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * PomodoroViewModel
 * - Timer dijalankan di ESP32, app hanya menampilkan state dari BLE
 * - App mengirim perintah ke ESP32 (start/pause/resume/reset)
 * - snackbarMessage: event satu-tembak untuk Snackbar di UI
 */
class PomodoroViewModel(
    private val bleService: BleService,
    private val repository: SessionRepository
) : ViewModel() {

    // ── Settings ──────────────────────────────────────────────────
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // ── IoT state (sumber kebenaran dari ESP32) ───────────────────
    val iotState: StateFlow<IoTState> = bleService.iotState

    // ── Air quality ────────────────────────────────────────────────
    private val _airQuality = MutableStateFlow(AirQualityData())
    val airQuality: StateFlow<AirQualityData> = _airQuality.asStateFlow()

    // ── Snackbar events (satu-tembak) ──────────────────────────────
    private val _snackbarMessage = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 4)
    val snackbarMessage: SharedFlow<SnackbarEvent> = _snackbarMessage.asSharedFlow()

    // ── Notification events ────────────────────────────────────────
    private val _notificationEvent = MutableSharedFlow<NotificationEvent>()
    val notificationEvent: SharedFlow<NotificationEvent> = _notificationEvent.asSharedFlow()

    private var prevEspPhase: EspPhase = EspPhase.IDLE
    private var prevEspRunning: Boolean = false

    init {
        viewModelScope.launch {
            repository.settings.collect { saved -> _settings.value = saved }
        }

        viewModelScope.launch {
            bleService.espMessage.collect { msg ->
                val now = currentTimeMillis()
                when (msg) {
                    is EspMessage.Status -> {
                        updateAirFromAdc(msg.adc, now)
                        handlePhaseTransition(msg)
                        prevEspPhase   = msg.phase
                        prevEspRunning = msg.running
                    }
                    is EspMessage.Air -> {
                        updateAirFromAdc(msg.adc, now)
                    }
                    is EspMessage.Sensor -> {
                        updateAirFromAdc(msg.adc, now)
                    }
                    is EspMessage.User -> {
                        if (msg.present) emitNotification(NotificationEvent.UserDetected)
                        else             emitNotification(NotificationEvent.UserLeft)
                    }
                    is EspMessage.Msg -> {
                        // Pesan teks dari ESP32 → tampilkan ke Snackbar
                        _snackbarMessage.emit(SnackbarEvent.Warning(msg.text))
                    }
                    is EspMessage.Unknown -> { /* abaikan */ }
                }
            }
        }

        // isLive dikelola oleh AirQualityViewModel — tidak perlu action di sini
    }

    // ── BLE ────────────────────────────────────────────────────────
    fun startBleConnection() {
        bleService.startScan(_settings.value.bleDeviceName)
    }

    fun stopBleConnection() {
        bleService.disconnect()
    }

    // ── Perintah Timer → dikirim ke ESP32 ────────────────────────
    fun sendStart() {
        if (!iotState.value.isConnected) {
            viewModelScope.launch {
                _snackbarMessage.emit(SnackbarEvent.Error("Hubungkan ke ESP32 terlebih dahulu"))
            }
            return
        }
        val s = _settings.value
        bleService.sendCommand(
            EspCommand.start(
                focusMin = s.focusDurationMinutes,
                shortMin = s.shortBreakMinutes,
                longMin  = s.longBreakMinutes,
                cycles   = s.sessionsBeforeLongBreak,
                volume   = s.volume
            )
        )
    }

    fun sendPause()  {
        if (iotState.value.isConnected) bleService.sendCommand(EspCommand.pause())
    }

    fun sendResume() {
        if (iotState.value.isConnected) bleService.sendCommand(EspCommand.resume())
    }

    fun sendReset()  {
        if (iotState.value.isConnected) bleService.sendCommand(EspCommand.reset())
    }

    fun sendVolume(value: Int) {
        val clamped = value.coerceIn(0, 30)
        bleService.sendCommand(EspCommand.volume(clamped))
        updateSettings(_settings.value.copy(volume = clamped))
    }

    fun sendAudio(track: Int) {
        if (track in 1..6) bleService.sendCommand(EspCommand.audio(track))
    }

    // ── Settings ───────────────────────────────────────────────────
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            _settings.value = newSettings
        }
    }

    fun syncConfigToEsp() {
        val s = _settings.value
        bleService.sendCommand(
            EspCommand.config(
                focusMin = s.focusDurationMinutes,
                shortMin = s.shortBreakMinutes,
                longMin  = s.longBreakMinutes,
                cycles   = s.sessionsBeforeLongBreak,
                volume   = s.volume
            )
        )
    }

    // ── Helpers ────────────────────────────────────────────────────
    private fun updateAirFromAdc(adc: Int, timestamp: Long) {
        val newAq = AirQualityData(adc = adc, timestamp = timestamp)
        _airQuality.value = newAq
        if (_settings.value.notifyAirQuality && newAq.level == AirQualityLevel.HAZARDOUS) {
            viewModelScope.launch {
                emitNotification(NotificationEvent.AirQualityBad)
            }
        }
    }

    private suspend fun handlePhaseTransition(msg: EspMessage.Status) {
        if (!_settings.value.notifySessionDone) return
        if (prevEspPhase == EspPhase.FOCUS && msg.phase != EspPhase.FOCUS && msg.phase != EspPhase.IDLE) {
            emitNotification(NotificationEvent.FocusSessionDone)
            saveSessionRecord(msg)
        }
        if ((prevEspPhase == EspPhase.SHORT_REST || prevEspPhase == EspPhase.LONG_REST)
            && msg.phase == EspPhase.FOCUS) {
            emitNotification(NotificationEvent.BreakDone)
        }
    }

    private fun saveSessionRecord(msg: EspMessage.Status) {
        viewModelScope.launch {
            val now = currentTimeMillis()
            val session = Session(
                id              = now.toString(),
                phase           = "FOCUS",
                startTime       = now - (_settings.value.focusDurationMinutes * 60 * 1000L),
                endTime         = now,
                durationSeconds = _settings.value.focusDurationMinutes * 60,
                completed       = true,
                autoStarted     = iotState.value.presenceStatus == PresenceStatus.PRESENT
            )
            repository.saveSession(session)
        }
    }

    private suspend fun emitNotification(event: NotificationEvent) {
        _notificationEvent.emit(event)
    }

    override fun onCleared() {
        super.onCleared()
        bleService.release()
    }
}

/** Event snackbar satu-tembak */
sealed class SnackbarEvent {
    data class Info(val message: String)    : SnackbarEvent()
    data class Success(val message: String) : SnackbarEvent()
    data class Warning(val message: String) : SnackbarEvent()
    data class Error(val message: String)   : SnackbarEvent()
}

sealed class NotificationEvent {
    data object FocusSessionDone    : NotificationEvent()
    data object BreakDone           : NotificationEvent()
    data object AirQualityBad       : NotificationEvent()
    data object AirQualityDangerous : NotificationEvent()
    data object UserDetected        : NotificationEvent()
    data object UserLeft            : NotificationEvent()
}
