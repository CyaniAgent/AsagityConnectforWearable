package com.asagity.connectwearable.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * SOS-pattern vibration for the receive-find state: ... --- ... looped
 * seamlessly via waveform repeat. Call [stop] when leaving the state.
 */
class FindHaptics(context: Context) {

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    fun startSosLoop() {
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(SOS_PATTERN, REPEAT_FROM_START))
    }

    fun stop() {
        vibrator.cancel()
    }

    private companion object {
        const val REPEAT_FROM_START = 0

        const val DOT = 150L
        const val DASH = 400L
        const val GAP = 150L
        const val LETTER_GAP = 350L
        const val END_PAUSE = 1000L

        // Alternating off/on durations; index 0 is the start delay.
        // S (...)  O (---)  S (...) then a pause before repeating.
        val SOS_PATTERN = longArrayOf(
            0,
            DOT, GAP, DOT, GAP, DOT, LETTER_GAP,
            DASH, GAP, DASH, GAP, DASH, LETTER_GAP,
            DOT, GAP, DOT, GAP, DOT, END_PAUSE
        )
    }
}
