package com.example.beetles.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.beetles.R
import android.media.RingtoneManager
import android.net.Uri

class SoundManager(private val context: Context) {
    private var beetleScreamPlayer: MediaPlayer? = null

    fun playBeetleScream() {
        try {
            beetleScreamPlayer?.release()

            beetleScreamPlayer = MediaPlayer.create(context, R.raw.zvuk_litvina)
            beetleScreamPlayer?.isLooping = true
            beetleScreamPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBeetleScream() {
        beetleScreamPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        beetleScreamPlayer = null
    }

    fun release() {
        stopBeetleScream()
    }
}
