package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import android.view.ViewGroup
import java.util.*

import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private var videoView : PlayerView? = null

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3PlayerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        //add video view
        videoView = buildVideoView()

        startVideo()

        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "68650da0324217d506bcc2d4"
        val channelSlug = "samplechannel"


        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(this)


        // Set up the ITGMedia3PlayerAdapter with your player view
        val adapter = ITGMedia3PlayerAdapter(playerView = videoView)
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            activity = this, //mandatory: fragment activity instance
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component

            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
        )


        // Add the ITG component to your view hierarchy
        binding.outerContainer.addView(mITGComponent, 0)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                    // Implement your own back press action here
                    finish()
                }
            }
        })

    }

    private fun restorePlaybackStateIfAny(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }
    }

    private fun setupFullscreenMode() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun startVideo() {
        prepareMediaForPlaying(Uri.parse(Const.VIDEO_URL))
        player?.playWhenReady = playWhenReady
        player?.seekTo(0, playbackPosition)
        player?.prepare()
    }

    @OptIn(UnstableApi::class)
    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(this, upstreamDataSourceFactory)

        defaultDataSourceFactory.createDataSource()

        val mediaSource: MediaSource =
            if (mediaSourceUri.lastPathSegment?.endsWith(".m3u8") == true) {
                HlsMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(mediaSourceUri)
                    )
            } else {
                ProgressiveMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(mediaSourceUri)
                    )
            }
        player?.setMediaSource(mediaSource)
    }

    @SuppressLint("InflateParams")
    private fun buildVideoView(): PlayerView {
        val videoView = layoutInflater.inflate(R.layout.styled_player_view, null, false) as PlayerView
        videoView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return videoView    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT)
            .setSeekForwardIncrementMs(SEEK_INCREMENT)
            .build()

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        videoView?.player = player
        this.player = player
    }

    private fun releasePlayer() {
        Log.d(this.javaClass.simpleName, "releasePlayer")
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            videoView?.player = null
            exoPlayer.release()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

        }
        player = null
    }

    override fun onResume() {
        super.onResume()
        if ((Build.VERSION.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            startVideo()
        }
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT > 23) {
            releasePlayer()
        }
        super.onStop()
    }

    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)

        // Saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.dispatchKeyEvent(event)
        // ... rest of your dispatchKeyEvent code
        return super.dispatchKeyEvent(event)
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyUp(keyCode, event)
        // ... rest of your onKeyUp code
        return super.onKeyUp(keyCode, event)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyDown(keyCode, event)
        // ... rest of your onKeyDown code
        return super.onKeyDown(keyCode, event)
    }


    companion object {
        private const val SEEK_INCREMENT = 10_000L
    }
}