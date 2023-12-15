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

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private var videoView : StyledPlayerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        //add video view
        videoView = buildVideoView()
        binding.outerContainer.addView(videoView)

        startVideo()
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
        videoView?.player = player
        this.player = player
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            videoView?.player = null
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
    }

    companion object {
        private const val SEEK_INCREMENT = 10_000L
    }
}