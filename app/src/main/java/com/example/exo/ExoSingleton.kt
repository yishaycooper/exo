package com.example.exo

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer

object ExoSingleton {
    var player: ExoPlayer? = null

    fun initialize(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
        return player!!
    }

    fun release() {
        player?.release()
        player = null
    }
}