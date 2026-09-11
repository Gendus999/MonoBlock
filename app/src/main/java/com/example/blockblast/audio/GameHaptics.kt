package com.example.blockblast.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class GameHaptics(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isHapticsEnabled: Boolean = true

    fun piecePickup() {
        if (!isHapticsEnabled) return
        vibrate(12, 50)
    }

    fun piecePlaced() {
        if (!isHapticsEnabled) return
        vibrate(25, 120)
    }

    fun blastClear(linesCount: Int) {
        if (!isHapticsEnabled) return
        val duration = (40 + linesCount * 25).coerceAtMost(160).toLong()
        val amplitude = (150 + linesCount * 25).coerceAtMost(255)
        vibrate(duration, amplitude)
    }

    fun gameOver() {
        if (!isHapticsEnabled) return
        vibrate(150, 180)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore any permission or device issues
        }
    }
}
