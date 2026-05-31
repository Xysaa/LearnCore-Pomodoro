package com.example.pomodoro.ble

import android.content.Context

object AppContext {
    lateinit var context: Context
}

actual fun createBleService(): BleService = BleServiceAndroid(AppContext.context)
