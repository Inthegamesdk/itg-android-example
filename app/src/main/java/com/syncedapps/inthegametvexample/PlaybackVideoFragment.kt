package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.syncedapps.inthegametv.integration.ITGExoLeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent

class PlaybackVideoFragment : VideoSupportFragment() {

    private var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var shouldNotShowControls = false

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGExoLeanbackPlayerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        play()

        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "68650da0324217d506bcc2d4"
        val channelSlug = "samplechannel"

        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(requireContext())


        // Set up the ITGExoLeanbackPlayerAdapter with your player view
        val adapter = ITGExoLeanbackPlayerAdapter(playerView = surfaceView)
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            root = requireView(), //mandatory: root view of the screen
            lifecycleOwner = viewLifecycleOwner, //mandatory: the view's lifecycle owner
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component

            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
        )


        // Add the ITG component to your view hierarchy
        (requireView() as ViewGroup).addView(mITGComponent, 0)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                    // Implement your own back press action here
                    requireActivity().finish()
                }
            }
        })

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
            play()
        }
    }

    /** Pauses the player.  */
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue?.isPlaying == true) {
            mPlayerGlue?.pause()
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }


    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        mPlayerAdapter = LeanbackPlayerAdapter(requireContext(), player, UPDATE_DELAY)
        mPlayerGlue =
            PlaybackTransportControlGlue(activity, mPlayerAdapter)
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true

        play()
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer?.release()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    private fun play(streamUrl: String? = Const.VIDEO_URL) {
        prepareMediaForPlaying(Uri.parse(streamUrl))
        mPlayerGlue?.play()
    }

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val userAgent: String = Util.getUserAgent(requireContext(), "VideoPlayerGlue")

        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(userAgent)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(requireContext(), upstreamDataSourceFactory)

        defaultDataSourceFactory.createDataSource()

        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(defaultDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(mediaSourceUri))

        mPlayer?.setMediaSource(mediaSource)
    }

    override fun showControlsOverlay(runAnimation: Boolean) {
        if (shouldNotShowControls) {
            shouldNotShowControls = false
        } else {
            super.showControlsOverlay(runAnimation)
        }
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
        private const val UPDATE_DELAY = 16
    }
}
