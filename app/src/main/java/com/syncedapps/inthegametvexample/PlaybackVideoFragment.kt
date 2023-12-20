package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragmentGlueHost
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
import com.syncedapps.inthegametv.domain.model.Storage
import com.syncedapps.inthegametv.domain.model.UserRole
import com.syncedapps.inthegametv.network.ITGEnvironment
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent
import android.view.ViewGroup

class PlaybackVideoFragment : VideoSupportFragment(), VideoPlayerGlue.OnActionClickedListener {

    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var shouldNotShowControls = false

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGExoLeanbackPlayerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        play()

        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "653647b68b8364785c095ae3"
        val channelSlug = "espn"


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
            extraDataSlug = null, //optional: secondary channel or category
            userBroadcasterForeignID = null, //optional: your user UUID
            userInitialName = null, //optional: viewer's name/nickname
            userRole = UserRole.USER, //optional: UserRole.USER, UserRole.GUEST
            userInitialAvatarUrl = null, //optional: viewer's avatar absolute url
            userEmail = null, //optional: viewer's email
            userPhone = null, //optional: viewer's phone
            language = null, //optional: 'en', 'es', 'he', 'ru'
            itgEnvironment = ITGEnvironment.v2_3, //optional: ITG stable environment route
            storage = Storage.CDN, //optional: Storage.CDN, Storage.BLOB
            webp = false //optional: use webp equivalents for images added via admin panel
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
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, this)
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

    //pass play/pause events to overlay so that it can track the video time
    override fun onPlayAction() {}

    override fun onPauseAction() {}

    override fun onPrevious() {}

    override fun onNext() {}

    override fun onMoreActions() {
        hideControlsOverlay(true)

        mITGComponent?.itgOverlayView?.openMenu()
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
