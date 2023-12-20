package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import java.util.*

import com.syncedapps.inthegametv.integration.ITGExoPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametv.domain.model.Storage
import com.syncedapps.inthegametv.domain.model.UserRole
import com.syncedapps.inthegametv.network.ITGEnvironment
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private var videoView : StyledPlayerView? = null

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGExoPlayerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        //add video view
        videoView = buildVideoView()

        startVideo()

        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(this)


        // Set up the ITGExoPlayerAdapter with your player view
        val adapter = ITGExoPlayerAdapter(playerView = videoView)
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            activity = this, //mandatory: fragment activity instance
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component

            accountId = Const.ACCOUNT_ID, //mandatory: your ITG accountId
            channelSlug = Const.CHANNEL_SLUG, //mandatory: your channelId on our admin panel
            extraDataSlug = null, //optional: secondary channel or category
            userBroadcasterForeignID = null, //optional: your user UUID
            userInitialName = null, //optional: viewer's name/nickname
            userRole = UserRole.USER, //optional: 'user', 'guest'
            userInitialAvatarUrl = null, //optional: viewer's avatar absolute url
            userEmail = null, //optional: viewer's email
            userPhone = null, //optional: viewer's phone
            language = null, //optional: 'en', 'es', 'he', 'ru'
            itgEnvironment = ITGEnvironment.v2_3, //optional: ITG stable environment route
            storage = Storage.CDN, //optional: 'Storage.CDN', 'Storage.BLOB'
            webp = false //optional: use webp equivalents for images added via admin panel
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

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(this, upstreamDataSourceFactory)

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
    private fun buildVideoView(): StyledPlayerView {
        val videoView = layoutInflater.inflate(R.layout.styled_player_view, null, false) as StyledPlayerView
        videoView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return videoView
    }

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
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            videoView?.player = null

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

            exoPlayer.release()
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