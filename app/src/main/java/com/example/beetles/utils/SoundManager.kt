package com.example.beetles.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

class SoundManager(private val context: Context) {
    private var beetleScreamPlayer: MediaPlayer? = null

    fun playBeetleScream() {
        try {
            beetleScreamPlayer?.release()

            val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            beetleScreamPlayer = MediaPlayer.create(context, notificationUri)
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
