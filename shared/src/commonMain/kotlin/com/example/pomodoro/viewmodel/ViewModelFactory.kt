package com.example.pomodoro.viewmodel

import com.example.pomodoro.ble.createBleService
import com.example.pomodoro.repository.SessionRepository
import com.example.pomodoro.repository.createDataStore

/**
 * Simple singleton container — menghindari DI framework eksternal.
 * Dibuat sekali di App entry point dan digunakan di semua screen.
 */
object ViewModelContainer {

    private val bleService by lazy { createBleService() }

    private val dataStore by lazy { createDataStore() }

    val repository by lazy { SessionRepository(dataStore) }

    val pomodoroViewModel by lazy {
        PomodoroViewModel(bleService, repository)
    }

    val statisticsViewModel by lazy {
        StatisticsViewModel(repository)
    }

    val airQualityViewModel by lazy {
        AirQualityViewModel(bleService)
    }

    val settingsViewModel by lazy {
        SettingsViewModel(repository, pomodoroViewModel)
    }
}
