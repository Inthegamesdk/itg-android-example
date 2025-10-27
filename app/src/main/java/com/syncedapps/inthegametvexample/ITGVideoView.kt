package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent

@SuppressLint("ViewConstructor")
@OptIn(UnstableApi::class)
class ITGVideoView(private val context : Context, private val videoUrl : String) : FrameLayout(context), LifecycleOwner {

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3PlayerAdapter? = null

    private var player: ExoPlayer? = null
    private val videoView : PlayerView = buildVideoView()

    // Lifecycle START
    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private fun onAppear() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    private fun onDisappear() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
    // Lifecycle END


    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onAppear()
        // Init ITG before player init
        initITG()
        initializePlayer()
        prepareMediaForPlaying(Uri.parse(videoUrl))
    }

    override fun onDetachedFromWindow() {
        releasePlayer()
        onDisappear()
        super.onDetachedFromWindow()
    }

    private fun initITG() {
        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "68650da0324217d506bcc2d4"
        val channelSlug = "samplechannel"


        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(context)

        // Set up the ITGMedia3PlayerAdapter with your player view
        val adapter = ITGMedia3PlayerAdapter(playerView = videoView)
        mITGPlayerAdapter = adapter

        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            root = this, // root container,
            lifecycleOwner = this, // lifecycle owner, might be your view lifecycle
            playerAdapter = adapter,
            accountId = accountId,
            channelSlug = channelSlug,
        )

        addView(mITGComponent, 0)
    }

    // Returns true when backpress is consumed by the view
    fun onBackPressed() : Boolean {
        return mITGComponent?.handleBackPressIfNeeded() != false
    }

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(context, upstreamDataSourceFactory)

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
        player?.prepare()
        player?.playWhenReady = true
    }

    @SuppressLint("InflateParams")
    private fun buildVideoView(): PlayerView {
        val videoView = LayoutInflater.from(context).inflate(R.layout.styled_player_view, null, false) as PlayerView
        videoView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return videoView    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val player = ExoPlayer.Builder(context).build()

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        videoView.player = player
        this.player = player
    }

    private fun releasePlayer() {
        Log.d(this.javaClass.simpleName, "releasePlayer")
        player?.let { exoPlayer ->
            videoView.player = null
            exoPlayer.release()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

        }
        player = null
    }

}