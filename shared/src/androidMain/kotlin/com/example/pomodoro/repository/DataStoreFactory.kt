package com.example.pomodoro.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.pomodoro.ble.AppContext

actual fun createDataStore(): DataStore<Preferences> {
    val context = AppContext.context
    return PreferenceDataStoreFactory.create(
        produceFile = {
            context.filesDir.resolve(DATASTORE_FILE)
        }
    )
}
