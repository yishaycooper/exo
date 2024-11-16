package com.example.exo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView


class MainActivity : AppCompatActivity() {

    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var player: ExoPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        styledPlayerView = findViewById(R.id.styled_player_view)
        player = ExoSingleton.initialize(this)

        styledPlayerView.player = player
        player.addListener(playerListener)


        val mediaItem = MediaItem.fromUri("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4")
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                // Player has started playback
                Log.d("tttt", "Playback started")
            } else {
                // Player has paused playback
                Log.d("tttt", "Playback paused")
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> Log.d("tttt", "Player is ready")
                Player.STATE_BUFFERING -> Log.d("ttt", "Player is buffering")
                Player.STATE_ENDED -> Log.d("tttt", "Playback ended")
                Player.STATE_IDLE -> Log.d("tttt", "Player is idle")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("tttt", "Error occurred: ${error.message}")
        }
    }


    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            ExoSingleton.release()
        } else {
            val serviceIntent = Intent(this, ExoService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent) // triggers onCreate in ExoService, which creates the notification.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.removeListener(playerListener) // Remove listener
        ExoSingleton.release()
    }


}










