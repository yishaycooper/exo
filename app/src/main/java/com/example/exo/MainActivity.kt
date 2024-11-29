package com.example.exo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.StyledPlayerView




//MediaSessionCompat with transportControl button and custom action
class MainActivity : AppCompatActivity() {

    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var player: ExoPlayer

    //    use for transportControl button
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var transportControls: MediaControllerCompat.TransportControls



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        styledPlayerView = findViewById(R.id.styled_player_view)
        player = ExoSingleton.initialize(this)

        styledPlayerView.player = player
        player.addListener(playerListener)


//    use for transportControl button
        mediaSession = MediaSessionCompat(this, "ExoSession")
        mediaSession.setCallback(mediaSessionCallback)
        mediaSession.isActive = true
        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        transportControls = mediaController.transportControls
        findViewById<Button>(R.id.play_pause_button).setOnClickListener {
            if (player.isPlaying) {
                transportControls.pause()
            } else {
                transportControls.play()
            }
        }

        findViewById<Button>(R.id.custom_button).setOnClickListener {
            mediaSession.controller.transportControls.sendCustomAction(
                "ACTION_BOOKMARK",
                null // Optional extras
            )
        }


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

    // use for transportControls
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            player.play()
        }

        override fun onPause() {
            super.onPause()
            player.pause()
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                "ACTION_BOOKMARK" -> {
                    val bookmarkPosition = player.currentPosition
                    Log.d("tttt", "Bookmark saved at position: $bookmarkPosition")
                    // Handle the bookmark action
                }
            }
        }


        // Handle other commands like stop, seek, etc.
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

//    use for transportControl button
        mediaSession.release()
    }
}









// using only TransportControls
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var styledPlayerView: StyledPlayerView
//    private lateinit var player: ExoPlayer
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaSessionConnector: MediaSessionConnector
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        styledPlayerView = findViewById(R.id.styled_player_view)
//        player = ExoSingleton.initialize(this)
//
//        styledPlayerView.player = player
//        player.addListener(playerListener)
//
//
//        mediaSession = MediaSessionCompat(this, "ExoSession")
//        mediaSession.isActive = true
//        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlayer(player)
//
//        findViewById<Button>(R.id.play_pause_button).setOnClickListener {
//            if (player.isPlaying) {
//                mediaSession.controller.transportControls.pause()
//            } else {
//                mediaSession.controller.transportControls.play()
//            }
//        }
//
//
//        val mediaItem = MediaItem.fromUri("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4")
//        player.setMediaItem(mediaItem)
//        player.prepare()
//        player.playWhenReady = true // to start playing right away
//    }
//
//    private val playerListener = object : Player.Listener {
//        override fun onIsPlayingChanged(isPlaying: Boolean) {
//            if (isPlaying) {
//                // Player has started playback
//                Log.d("tttt", "Playback started")
//            } else {
//                // Player has paused playback
//                Log.d("tttt", "Playback paused")
//            }
//        }
//
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            when (playbackState) {
//                Player.STATE_READY -> Log.d("tttt", "Player is ready")
//                Player.STATE_BUFFERING -> Log.d("ttt", "Player is buffering")
//                Player.STATE_ENDED -> Log.d("tttt", "Playback ended")
//                Player.STATE_IDLE -> Log.d("tttt", "Player is idle")
//            }
//        }
//
//        override fun onPlayerError(error: PlaybackException) {
//            Log.e("tttt", "Error occurred: ${error.message}")
//        }
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        if (isFinishing) {
//            ExoSingleton.release()
//        } else {
//            val serviceIntent = Intent(this, ExoService::class.java)
//            ContextCompat.startForegroundService(this, serviceIntent) // triggers onCreate in ExoService, which creates the notification.
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.removeListener(playerListener) // Remove listener
//        ExoSingleton.release()
//        mediaSession.release()
//    }
//}





// MediaSessionCompat with transportControl button
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var styledPlayerView: StyledPlayerView
//    private lateinit var player: ExoPlayer
//
////    use for transportControl button
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaController: MediaControllerCompat
//    private lateinit var transportControls: MediaControllerCompat.TransportControls
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        styledPlayerView = findViewById(R.id.styled_player_view)
//        player = ExoSingleton.initialize(this)
//
//        styledPlayerView.player = player
//        player.addListener(playerListener)
//
//
////    use for transportControl button
//        mediaSession = MediaSessionCompat(this, "ExoSession")
//        mediaSession.setCallback(mediaSessionCallback)
//        mediaSession.isActive = true
//        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
//        transportControls = mediaController.transportControls
//        val playPauseButton: Button = findViewById(R.id.play_pause_button)
//        playPauseButton.setOnClickListener {
//            if (player.isPlaying) {
//                transportControls.pause()
//            } else {
//                transportControls.play()
//            }
//        }
//
//
//        val mediaItem = MediaItem.fromUri("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4")
//        player.setMediaItem(mediaItem)
//        player.prepare()
//        player.playWhenReady = true
//    }
//
//    private val playerListener = object : Player.Listener {
//        override fun onIsPlayingChanged(isPlaying: Boolean) {
//            if (isPlaying) {
//                // Player has started playback
//                Log.d("tttt", "Playback started")
//            } else {
//                // Player has paused playback
//                Log.d("tttt", "Playback paused")
//            }
//        }
//
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            when (playbackState) {
//                Player.STATE_READY -> Log.d("tttt", "Player is ready")
//                Player.STATE_BUFFERING -> Log.d("ttt", "Player is buffering")
//                Player.STATE_ENDED -> Log.d("tttt", "Playback ended")
//                Player.STATE_IDLE -> Log.d("tttt", "Player is idle")
//            }
//        }
//
//        override fun onPlayerError(error: PlaybackException) {
//            Log.e("tttt", "Error occurred: ${error.message}")
//        }
//    }
//
//    // use for transportControls
//    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
//        override fun onPlay() {
//            super.onPlay()
//            player.play()
//        }
//
//        override fun onPause() {
//            super.onPause()
//            player.pause()
//        }
//        // Handle other commands like stop, seek, etc.
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        if (isFinishing) {
//            ExoSingleton.release()
//        } else {
//            val serviceIntent = Intent(this, ExoService::class.java)
//            ContextCompat.startForegroundService(this, serviceIntent) // triggers onCreate in ExoService, which creates the notification.
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.removeListener(playerListener) // Remove listener
//        ExoSingleton.release()
//
////    use for transportControl button
//        mediaSession.release()
//    }
//}




// TransportControls
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var styledPlayerView: StyledPlayerView
//    private lateinit var player: ExoPlayer
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaController: MediaControllerCompat
//    private lateinit var transportControls: MediaControllerCompat.TransportControls
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        styledPlayerView = findViewById(R.id.styled_player_view)
//        player = ExoSingleton.initialize(this)
//
//        styledPlayerView.player = player
//        player.addListener(playerListener)
//
//
//        mediaSession = MediaSessionCompat(this, "ExoSession").apply {
//            setCallback(mediaSessionCallback)
//            isActive = true
//        }
//
//        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
//        transportControls = mediaController.transportControls
//
//        findViewById<Button>(R.id.play_pause_button).setOnClickListener {
//            if (player.isPlaying) {
//                transportControls.pause()
//            } else {
//                transportControls.play()
//            }
//        }
//
//        val mediaItem = MediaItem.fromUri("https://live-par-2-abr.livepush.io/vod/bigbuckbunnyclip.mp4")
//        player.setMediaItem(mediaItem)
//        player.prepare()
//        player.playWhenReady = true
//    }
//
//
//    private val playerListener = object : Player.Listener {
//        override fun onIsPlayingChanged(isPlaying: Boolean) {
//            if (isPlaying) {
//                // Player has started playback
//                Log.d("tttt", "Playback started")
//            } else {
//                // Player has paused playback
//                Log.d("tttt", "Playback paused")
//            }
//        }
//
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            when (playbackState) {
//                Player.STATE_READY -> Log.d("tttt", "Player is ready")
//                Player.STATE_BUFFERING -> Log.d("ttt", "Player is buffering")
//                Player.STATE_ENDED -> Log.d("tttt", "Playback ended")
//                Player.STATE_IDLE -> Log.d("tttt", "Player is idle")
//            }
//        }
//
//        override fun onPlayerError(error: PlaybackException) {
//            Log.e("tttt", "Error occurred: ${error.message}")
//        }
//    }
//
//    // use for transportControls
//    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
//        override fun onPlay() {
//            super.onPlay()
//            player.play()
//        }
//
//        override fun onPause() {
//            super.onPause()
//            player.pause()
//        }
//        // Handle other commands like stop, seek, etc.
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        if (isFinishing) {
//            ExoSingleton.release()
//        } else {
//            val serviceIntent = Intent(this, ExoService::class.java)
//            ContextCompat.startForegroundService(this, serviceIntent) // triggers onCreate in ExoService, which creates the notification.
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        player.removeListener(playerListener) // Remove listener
//        ExoSingleton.release()
//
//        mediaSession.release()
//    }
//
//
//}







// MediaBrowserServiceCompat Used for audio not video
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var styledPlayerView: StyledPlayerView
//    private var mediaController: MediaControllerCompat? = null
//
//    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallBack(this)
//    private lateinit var mediaBrowser: MediaBrowserCompat
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        styledPlayerView = findViewById(R.id.styled_player_view)
//
//        // Initialize MediaBrowser
//        mediaBrowser = MediaBrowserCompat(
//            this,
//            ComponentName(this, ExoBrowserService::class.java),
//            mediaBrowserConnectionCallback,
//            null
//        )
//    }
//
//    override fun onStart() {
//        super.onStart()
//        mediaBrowser.connect() // Connect to MediaBrowserService
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mediaController?.unregisterCallback(mediaControllerCallback)
//        mediaBrowser.disconnect() // Disconnect from MediaBrowserService
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        styledPlayerView.player = null
//        ExoSingleton.release()
//    }
//
//    // MediaBrowser Connection Callback
//    private inner class MediaBrowserConnectionCallBack(private val context: Context) : MediaBrowserCompat.ConnectionCallback() {
//        override fun onConnected() {
//            // Obtain the session token from the service
//            val sessionToken = mediaBrowser.sessionToken
//
//            // Create MediaControllerCompat for interacting with the MediaBrowserService
//            mediaController = MediaControllerCompat(context, sessionToken).apply {
//                registerCallback(mediaControllerCallback)
//            }
//
//            // Set the MediaControllerCompat as the active controller for media session
//            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
//
//            // Update UI (if needed)
//            updateUI()
//        }
//
//        override fun onConnectionSuspended() {
//            Log.e("MediaBrowser", "Connection Suspended")
//            mediaController = null
//        }
//
//        override fun onConnectionFailed() {
//            Log.e("MediaBrowser", "Connection Failed")
//        }
//    }
//
//    // MediaController Callback
//    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            state?.let {
//                Log.d("MediaController", "Playback state changed: ${state.state}")
//                // Update play/pause button or other UI elements here
//            }
//        }
//
//        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//            metadata?.let {
//                Log.d("MediaController", "Metadata changed: ${metadata.description.title}")
//                // Update UI with new metadata (e.g., song title, artist)
//            }
//        }
//    }
//
//    // Example of updating the UI when the MediaController connects
//    private fun updateUI() {
//        val transportControls = mediaController?.transportControls
//
//        // Example: Play/Pause button click listener
//        findViewById<Button>(R.id.play_pause_button).setOnClickListener {
//            val playbackState = mediaController?.playbackState
//            when (playbackState?.state) {
//                PlaybackStateCompat.STATE_PLAYING -> transportControls?.pause()
//                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> transportControls?.play()
//                else -> Log.d("MediaController", "Unhandled playback state: ${playbackState?.state}")
//            }
//        }
//    }
//}








// MediaBrowserServiceCompat Used for audio not video
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var styledPlayerView: StyledPlayerView
//
//
//    lateinit var mediaControllerCompat: MediaControllerCompat
//
//    private val mediaBrowserServiceCallback =
//        MediaBrowserConnectionCallBack(this)
//    private val mediaBrowser = MediaBrowserCompat(
//        this,
//        ComponentName(this, ExoBrowserService::class.java),
//        mediaBrowserServiceCallback,
//        null
//
//    ).apply {
//        connect()
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        styledPlayerView = findViewById(R.id.styled_player_view)
//
//        customButton()
//
//
//    }
//
//    private fun customButton() {
//        val playPauseButton: Button = findViewById(R.id.play_pause_button)
//        playPauseButton.setOnClickListener {
//            mediaController?.let { controller ->
//                val playbackState = controller.playbackState
//                when (playbackState?.state) {
//                    PlaybackStateCompat.STATE_PLAYING -> {
//                        controller.transportControls.pause()
//                    }
//                    PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
//                        controller.transportControls.play()
//                    }
//                    else -> Log.e("tttt", "Unhandled playback state: ${playbackState?.state}")
//                }
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        mediaBrowser?.connect() // Connect to MediaBrowserService
//    }
//
//    override fun onStop() {
//        super.onStop()
////        mediaController?.unregisterCallback(mediaControllerCallback)
//        mediaBrowser?.disconnect() // Disconnect from MediaBrowserService
//    }
//
//    private val mediaBrowserConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
//        override fun onConnected() {
//            mediaControllerCompat = MediaControllerCompat(
//                applicationContext,
//                mediaBrowser.sessionToken
//            ).apply {
//                registerCallback(MediaControllerCallBack())
//            }
//        }
//
//        override fun onConnectionSuspended() {
//            Log.e("tttt", "MediaBrowser connection suspended")
//        }
//
//        override fun onConnectionFailed() {
//            Log.e("tttt", "MediaBrowser connection failed")
//        }
//    }
//
//    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            state?.let {
//                Log.d("tttt", "Playback state changed: ${state.state}")
//            }
//        }
//
//        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//            metadata?.let {
//                Log.d("tttt", "Metadata changed: ${metadata.description.title}")
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        styledPlayerView.player = null
//        ExoSingleton.release()
//    }
//
//    private inner class MediaControllerCallBack : MediaControllerCompat.Callback() {
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            super.onPlaybackStateChanged(state)
//        }
//        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//            super.onMetadataChanged(metadata)
//
//        }
//
//        override fun onSessionDestroyed() {
//            super.onSessionDestroyed()
//            mediaBrowserServiceCallback.onConnectionSuspended()
//        }
//    }
//
//    private inner class MediaBrowserConnectionCallBack(
//        private val context: Context
//    ) : MediaBrowserCompat.ConnectionCallback() {
//
//        override fun onConnected() {
//
//            mediaControllerCompat = MediaControllerCompat(
//                context,
//                mediaBrowser.sessionToken
//            ).apply {
//                registerCallback(MediaControllerCallBack())
//            }
//        }
//
//        override fun onConnectionSuspended() {
//
//        }
//
//        override fun onConnectionFailed() {
//
//        }
//    }
//
//
//
//}































