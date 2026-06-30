package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class WakeAlertEngine(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    init {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startAlert(intensity: String, alertType: String) {
        Log.d("WakeAlertEngine", "startAlert: intensity=$intensity, alertType=$alertType")
        
        // 1. Play Sound if configured (SOUND_VIB or SOUND_ONLY)
        if (alertType == "SOUND_VIB" || alertType == "SOUND_ONLY") {
            try {
                if (mediaPlayer == null) {
                    val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, alertUri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                }
            } catch (e: Exception) {
                Log.e("WakeAlertEngine", "Error playing sound", e)
            }
        }

        // 2. Vibrate if configured (SOUND_VIB or VIBRATE_ONLY)
        if (alertType == "SOUND_VIB" || alertType == "VIBRATE_ONLY") {
            try {
                vibrator?.let { vib ->
                    if (vib.hasVibrator()) {
                        val pattern = when (intensity) {
                            "HIGH" -> longArrayOf(0, 800, 200, 800, 200)
                            "MEDIUM" -> longArrayOf(0, 500, 300, 500, 300)
                            else -> longArrayOf(0, 300, 500, 300, 500) // LOW
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val amplitudes = when (intensity) {
                                "HIGH" -> intArrayOf(0, 255, 0, 255, 0)
                                "MEDIUM" -> intArrayOf(0, 180, 0, 180, 0)
                                else -> intArrayOf(0, 100, 0, 100, 0)
                            }
                            vib.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0))
                        } else {
                            @Suppress("DEPRECATION")
                            vib.vibrate(pattern, 0)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WakeAlertEngine", "Error starting vibration", e)
            }
        }
    }

    fun stopAlert() {
        Log.d("WakeAlertEngine", "stopAlert")
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("WakeAlertEngine", "Error stopping mediaPlayer", e)
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("WakeAlertEngine", "Error cancelling vibration", e)
        }
    }
}
