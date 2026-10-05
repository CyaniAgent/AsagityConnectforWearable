package com.asagity.connectwearable.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.SoundPool

/**
 * Seamlessly loops a short asset sound (SoundPool loop = -1).
 * One instance per find session; call [release] when the screen leaves.
 */
class FindAudioPlayer(private val appContext: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .build()
        .also { pool ->
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == soundId) {
                    streamId = pool.play(sampleId, 1f, 1f, 1, LOOP_FOREVER, 1f)
                }
            }
        }

    private var descriptor: AssetFileDescriptor? = null
    private var soundId = 0
    private var streamId = 0

    fun startLoop(assetPath: String) {
        stop()
        try {
            descriptor?.close()
            descriptor = appContext.assets.openFd(assetPath)
            soundId = soundPool.load(descriptor, 1)
        } catch (_: Exception) {
            // TODO: surface audio errors to the debug screen.
        }
    }

    fun stop() {
        if (streamId != 0) {
            soundPool.stop(streamId)
            streamId = 0
        }
    }

    fun release() {
        stop()
        try {
            descriptor?.close()
        } catch (_: Exception) {
            // Ignored on release path.
        } finally {
            descriptor = null
        }
        soundPool.release()
    }

    private companion object {
        const val LOOP_FOREVER = -1
    }
}
