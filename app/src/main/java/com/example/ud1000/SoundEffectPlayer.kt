package com.example.ud1000

import android.content.Context
import android.media.MediaPlayer

class SoundEffectPlayer(context: Context, resId: Int) {

    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, resId)

    fun play() {
        mediaPlayer?.let {
            it.seekTo(0)
            it.start()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare() // Prepare it for future playback
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        fun newInstance(context: Context, resId: Int): SoundEffectPlayer {
            return SoundEffectPlayer(context, resId)
        }
    }
}