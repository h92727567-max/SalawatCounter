package com.salawat.counter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.salawat.counter.service.SalawatRecognitionService
import com.salawat.counter.ui.theme.SalawatCounterTheme
import com.salawat.counter.ui.screens.HomeScreen
import com.salawat.counter.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (granted) {
            startRecognitionService()
        }
    }

    private val detectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SalawatRecognitionService.ACTION_DETECTED) {
                viewModel.incrementManual()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter(SalawatRecognitionService.ACTION_DETECTED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(detectionReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(detectionReceiver, filter)
        }

        setContent {
            SalawatCounterTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onToggleListening = { active ->
                        if (active) {
                            checkPermissionsAndStart()
                        } else {
                            stopRecognitionService()
                            viewModel.setListening(false)
                        }
                    }
                )
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startRecognitionService()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startRecognitionService() {
        val intent = Intent(this, SalawatRecognitionService::class.java).apply {
            action = SalawatRecognitionService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        viewModel.setListening(true)
    }

    private fun stopRecognitionService() {
        val intent = Intent(this, SalawatRecognitionService::class.java).apply {
            action = SalawatRecognitionService.ACTION_STOP
        }
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(detectionReceiver)
    }
}
