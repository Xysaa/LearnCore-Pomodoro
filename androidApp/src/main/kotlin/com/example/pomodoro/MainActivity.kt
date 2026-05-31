package com.example.pomodoro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pomodoro.ble.AppContext
import com.example.pomodoro.viewmodel.ViewModelContainer

class MainActivity : ComponentActivity() {

    // ── BLE Permission Launcher ────────────────────────────────────────────
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            // Permission baru saja di-grant — mulai scan sekarang
            ViewModelContainer.pomodoroViewModel.startBleConnection()
        } else {
            Toast.makeText(
                this,
                "Izin Bluetooth diperlukan untuk koneksi IoT",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Init AppContext singleton untuk BLE & DataStore
        AppContext.context = applicationContext

        setContent {
            App()
        }

        // Request permission SETELAH setContent, lalu scan di callback
        requestBlePermissionsOrScan()
    }

    private fun requestBlePermissionsOrScan() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            // Semua permission sudah ada — langsung scan
            ViewModelContainer.pomodoroViewModel.startBleConnection()
        } else {
            // Minta permission dulu, scan akan dilakukan di callback
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}
