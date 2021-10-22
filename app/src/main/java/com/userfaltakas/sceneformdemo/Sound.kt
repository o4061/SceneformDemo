package com.userfaltakas.sceneformdemo

import android.content.Context
import android.media.MediaPlayer

class Sound(private val context: Context) {
    private var mediaPlayer = MediaPlayer()

    fun startMediaPlayer() {
        mediaPlayer = MediaPlayer.create(context, R.raw.book1)
        mediaPlayer.isLooping = false
        mediaPlayer.setVolume(0.5f, 0.5f)
        mediaPlayer.start()
    }

    fun pause() {
        if (mediaPlayer.isPlaying)
            mediaPlayer.pause()
    }

    fun start() {
        if (!mediaPlayer.isPlaying)
            mediaPlayer.start()
    }

    fun changeVolume(leftVol: Float, rightVol: Float) {
        mediaPlayer.setVolume(leftVol, rightVol)
    }
}