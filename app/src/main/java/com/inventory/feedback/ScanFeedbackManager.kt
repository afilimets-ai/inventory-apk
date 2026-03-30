package com.inventory.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Success: короткий один імпульс 80ms
    private val successVibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
    } else null

    // Error: два імпульси 100ms + пауза 120ms + 100ms
    private val errorVibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        VibrationEffect.createWaveform(longArrayOf(0, 100, 120, 100), -1)
    } else null

    fun onScanSuccess() {
        playTone(ToneGenerator.TONE_PROP_BEEP, durationMs = 120)
        vibrate(successVibration, longArrayOf(0, 80))
    }

    fun onScanError() {
        playTone(ToneGenerator.TONE_PROP_NACK, durationMs = 200)
        vibrate(errorVibration, longArrayOf(0, 100, 120, 100))
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            toneGen.startTone(toneType, durationMs)
        } catch (_: Exception) {
            // ToneGenerator може не спрацювати на деяких пристроях — ігноруємо
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(effect: VibrationEffect?, pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && effect != null) {
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {
            // Деякі пристрої можуть не мати вібратора
        }
    }
}
