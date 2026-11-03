package com.salawat.counter.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.salawat.counter.MainActivity
import com.salawat.counter.R
import kotlinx.coroutines.*
import java.util.Locale

class SalawatRecognitionService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastDetectionTime = 0L
    private var isListening = false

    companion object {
        const val CHANNEL_ID = "salawat_recognition"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_DETECTED = "com.salawat.counter.DETECTED"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startListening()
            ACTION_STOP -> stopListening()
        }
        return START_STICKY
    }

    private fun startListening() {
        if (isListening) return
        isListening = true
        startForeground(NOTIFICATION_ID, createNotification("در حال گوش دادن..."))
        initializeRecognizer()
        startRecognition()
    }

    private fun stopListening() {
        isListening = false
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun initializeRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    if (isListening) {
                        serviceScope.launch {
                            delay(300)
                            startRecognition()
                        }
                    }
                }

                override fun onError(error: Int) {
                    if (isListening && error != SpeechRecognizer.ERROR_CLIENT) {
                        serviceScope.launch {
                            delay(800)
                            startRecognition()
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                    processResults(matches, confidences)
                    if (isListening) {
                        serviceScope.launch {
                            delay(400)
                            startRecognition()
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    private fun processResults(matches: ArrayList<String>?, confidences: FloatArray?) {
        if (matches.isNullOrEmpty()) return

        val minConfidence = 0.60f
        val debounceMs = 2000L

        matches.forEachIndexed { index, text ->
            val confidence = confidences?.getOrNull(index) ?: 0.7f
            if (confidence >= minConfidence && isSalawatPhrase(text)) {
                val now = System.currentTimeMillis()
                if (now - lastDetectionTime >= debounceMs) {
                    lastDetectionTime = now
                    onSalawatDetected()
                }
            }
        }
    }

    private fun isSalawatPhrase(text: String): Boolean {
        val normalized = text
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .lowercase(Locale("ar"))
            .trim()

        val patterns = listOf(
            "اللهم صل على محمد",
            "اللهم صل علي محمد",
            "اللهم صلي على محمد",
            "اللهم صلي علي محمد",
            "صل على محمد و آل محمد",
            "صلي على محمد و آل محمد"
        )

        return patterns.any { normalized.contains(it) } ||
                (normalized.contains("اللهم") && normalized.contains("صل") && normalized.contains("محمد"))
    }

    private fun onSalawatDetected() {
        sendBroadcast(Intent(ACTION_DETECTED))

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("صلوات تشخیص داده شد"))
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("صلوات‌شمار هوشمند")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تشخیص صلوات",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "اعلان دائمی هنگام تشخیص صوتی"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopListening()
        serviceScope.cancel()
        super.onDestroy()
    }
}
