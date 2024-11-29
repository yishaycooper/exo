package com.example.exo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager


class ExoService : Service() {
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private val CHANNEL_ID = "exo_channel_id"
    private val CHANNEL_NAME = "exo channel name"
    private val NOTIFICATION_ID = 101010

    private lateinit var notificationManager: NotificationManager
    private lateinit var player: ExoPlayer

    private lateinit var playbackStateReceiver: BroadcastReceiver


    override fun onCreate()     {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "ExoService").apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }

        player = ExoSingleton.initialize(this)

        playerNotificationManager = PlayerNotificationManager.Builder(
            this, NOTIFICATION_ID, CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return "Exo Title"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(this@ExoService, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        this@ExoService, 0, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return "Sample Subtitle"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return BitmapFactory.decodeResource(resources, R.drawable.ic_anchor)
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) startForeground(notificationId, notification)
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }
            })
            .build()

        // associates the media session with the notification.
        // This enables the notification's media controls (play, pause, skip) to trigger the callback methods you defined in MediaSessionCompat.Callback
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)
        playerNotificationManager.setPlayer(player) // allow trigger of PlayerNotificationManager.NotificationListener

        playbackStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.your.package.PLAYBACK_STATE_CHANGED") {
                    val isPlaying = intent.getBooleanExtra("isPlaying", false)
                    if (isPlaying) {
                        player.play()
                    } else {
                        player.pause()
                    }
                    // Update notification
                    playerNotificationManager.invalidate()
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            playbackStateReceiver,
            IntentFilter("com.example.exo.PLAYBACK_STATE_CHANGED")
        )

        // Add player listener to sync state
        player.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            if (playWhenReady) PlaybackStateCompat.STATE_PLAYING
                            else PlaybackStateCompat.STATE_PAUSED,
                            player.currentPosition,
                            1f
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                        .build()
                )
            }
        })
    }


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            player.play()
            Log.d("tttt", "onPlay()")
        }

        override fun onPause() {
            player.pause()
            Log.d("tttt", "onPause()")
        }

        override fun onStop() {
            player.stop()
            Log.d("tttt", "onStop()")
        }

        override fun onSkipToNext() {
            // Implement if you have next track functionality
            Log.d("tttt", "onSkipToNext()")
        }

        override fun onSkipToPrevious() {
            // Implement if you have previous track functionality
            Log.d("tttt", "onSkipToPrevious()")
        }
        // Optionally handle custom actions if needed
        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            Log.d("tttt", "onCustomAction()")
        }

    }



    override fun onDestroy() {
        playerNotificationManager.setPlayer(null)
        mediaSession.release()
        ExoSingleton.release()

        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(playbackStateReceiver)
        super.onDestroy()

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}




