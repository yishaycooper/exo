package com.example.exo


import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

// MediaBrowserServiceCompat Used for audio not video
class ExoBrowserService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private var player: ExoPlayer? = null

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (clientPackageName == "com.example.exo") {
            // Allow client to connect, specifying a root ID
            BrowserRoot("trusted_root_id", null)
        } else {
            null // Deny access to untrusted clients
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // Empty for now
        result.sendResult(ArrayList())
    }

    override fun onCreate() {
        super.onCreate()

        // Create and set the MediaSession
        mediaSession = MediaSessionCompat(this, "ExoBrowserService")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Set the session's token to allow clients (e.g., MainActivity) to connect
        sessionToken = mediaSession.sessionToken

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        player?.setMediaSource(buildMediaSource()) // Use your media source here
        player?.prepare()

        player?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    // The player is ready and should be playing
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("tttt", "Player error: ${error.message}")

            }
        })

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                if (player?.playbackState == Player.STATE_READY) {
                    player?.play()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                } else {
                    player?.prepare()
                }
            }

            override fun onPause() {
                super.onPause()
                player?.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            override fun onStop() {
                super.onStop()
                player?.stop()
                updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
            }
        })



        // Update the playback state
        val state = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
            .build()
        mediaSession.setPlaybackState(state)

        // Set the MediaSession's callback for transport controls
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                player?.play()
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }

            override fun onPause() {
                super.onPause()
                player?.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            override fun onStop() {
                super.onStop()
                player?.stop()
                updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        mediaSession.release()
    }

    private fun buildMediaSource(): MediaSource {
        // Provide a simple media source (e.g., HLS, MP4, etc.)
        val dataSourceFactory = DefaultDataSourceFactory(this, "ExoBrowserService")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"))
//            .createMediaSource(MediaItem.fromUri("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4"))
//            .createMediaSource(MediaItem.fromUri("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"))
    }

    private fun updatePlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setState(state, player?.currentPosition ?: 0, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }



}


// MediaBrowserServiceCompat Used for audio not video
//class ExoBrowserService : MediaBrowserServiceCompat() {
//
//    private lateinit var playerNotificationManager: PlayerNotificationManager
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaSessionConnector: MediaSessionConnector
//    private val CHANNEL_ID = "exo_channel_id"
//    private val CHANNEL_NAME = "exo channel name"
//    private val NOTIFICATION_ID = 101010
//
//    private lateinit var player: ExoPlayer
//
//    override fun onGetRoot(
//        clientPackageName: String,
//        clientUid: Int,
//        rootHints: Bundle?
//    ): BrowserRoot? {
//        return if (clientPackageName == "com.example.exo") {
//            // Allow client to connect, specifying a root ID
//            BrowserRoot("trusted_root_id", null)
//        } else {
//            null // Deny access to untrusted clients
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Initialize the ExoPlayer
//        player = ExoSingleton.initialize(this)
//
//        // Set up MediaSession
//        mediaSession = MediaSessionCompat(this, "ExoService").apply {
//            setCallback(mediaSessionCallback)
//            isActive = true
//        }
//        sessionToken = mediaSession.sessionToken // Required for MediaBrowserServiceCompat
//
//        // Set up MediaSessionConnector
//        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlayer(player)
//
//        // Set up Notification Manager
//        playerNotificationManager = PlayerNotificationManager.Builder(
//            this, NOTIFICATION_ID, CHANNEL_ID
//        )
//            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
//                override fun getCurrentContentTitle(player: Player): CharSequence {
//                    return "Exo Title"
//                }
//
//                override fun createCurrentContentIntent(player: Player): PendingIntent? {
//                    val intent = Intent(this@ExoBrowserService, MainActivity::class.java)
//                    return PendingIntent.getActivity(
//                        this@ExoBrowserService, 0, intent, PendingIntent.FLAG_IMMUTABLE
//                    )
//                }
//
//                override fun getCurrentContentText(player: Player): CharSequence? {
//                    return "Sample Subtitle"
//                }
//
//                override fun getCurrentLargeIcon(
//                    player: Player,
//                    callback: PlayerNotificationManager.BitmapCallback
//                ): Bitmap? {
//                    return BitmapFactory.decodeResource(resources, R.drawable.ic_anchor)
//                }
//            })
//            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
//                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
//                    if (ongoing) startForeground(notificationId, notification)
//                }
//
//                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//                    stopSelf()
//                }
//            })
//            .build()
//
//        // Connect MediaSession to NotificationManager
//        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)
//        playerNotificationManager.setPlayer(player)
//    }
//
//    override fun onDestroy() {
//        playerNotificationManager.setPlayer(null)
//        mediaSession.release()
//        ExoSingleton.release()
//        super.onDestroy()
//    }
//
//    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
//        override fun onPlay() {
//            if (!player.isPlaying) {
//                player.playWhenReady = true
//            }
//        }
//
//        override fun onPause() {
//            if (player.isPlaying) {
//                player.playWhenReady = false
//            }
//        }
//    }
//
//
//    override fun onLoadChildren(
//        parentId: String,
//        result: Result<List<MediaBrowserCompat.MediaItem>>
//    ) {
//        // Provide media items to media browser clients
//        val mediaItems = listOf(
//            MediaBrowserCompat.MediaItem(
//                MediaDescriptionCompat.Builder()
//                    .setMediaId("media1")
//                    .setTitle("Big Buck Bunny")
//                    .setDescription("Sample Video")
//                    .setMediaUri(Uri.parse("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4"))
//                    .build(),
//                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//            )
//        )
//        result.sendResult(mediaItems)
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return super.onBind(intent)
//    }
//
//}
