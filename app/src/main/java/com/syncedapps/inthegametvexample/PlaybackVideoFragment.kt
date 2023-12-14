package com.syncedapps.inthegametvexample

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragment
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
import com.syncedapps.inthegametv.ITGSettings
import com.syncedapps.inthegametv.data.CloseOption
import com.syncedapps.inthegametv.domain.model.AnalyticsEventSnapshot
import com.syncedapps.inthegametv.domain.model.UserSnapshot
import com.syncedapps.inthegametv.integration.ITGExoLeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import java.util.*


/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment(), VideoPlayerGlue.OnActionClickedListener {

    private var mITGComponent: ITGLeanbackComponent? = null
    private var mITGPlayerAdapter: ITGExoLeanbackPlayerAdapter? = null

    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var shouldNotShowControls = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        // create the overlay
        val adapter = ITGExoLeanbackPlayerAdapter(
            playerView = surfaceView
        )
        mITGPlayerAdapter = adapter
        mITGComponent = ITGLeanbackComponent(requireContext())
        mITGComponent?.init(
            requireView(),
            viewLifecycleOwner,
            adapter,
            Const.ACCOUNT_ID,
            Const.CHANNEL_SLUG,
            language = Const.LANGUAGE,
            userBroadcasterForeignID = "android_${Date().time}",
        )
        (requireView() as ViewGroup).addView(mITGComponent, 0)
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
        mITGComponent?.onSaveInstanceState(outState)
    }


    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player
        mITGPlayerAdapter?.onPlayerReady(player)
        mPlayerAdapter = LeanbackPlayerAdapter(requireContext(), player, UPDATE_DELAY)
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, this)
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mITGPlayerAdapter?.onPlayerReleased()
            mPlayer?.release()
            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    private fun play(streamUrl: String?) {
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

    // let the overlay handle the back button press if it needs to
    // (to dismiss interactions)
    fun handleBackPressIfNeeded(): Boolean {
        return mITGComponent?.handleBackPressIfNeeded() ?: false
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


    inner class ITGLeanbackComponent : ITGPlaybackComponent {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        override fun channelInfoDidLoad(streamUrl: String?) {
            super.channelInfoDidLoad(streamUrl)
            play(streamUrl)
        }

        //optional
        override fun overlayReceivedDeeplink(customUrl: String) {
            when (customUrl) {
                "next channel" -> {
                   //TODO
                }

                "previous channel" -> {
                    //TODO
                }

                else -> {
                    Toast.makeText(requireContext(), customUrl, Toast.LENGTH_LONG).show()
                }
            }
        }

        //optional
        override fun overlayProducedAnalyticsEvent(eventSnapshot: AnalyticsEventSnapshot) {
            Log.d(
                this.javaClass.simpleName,
                "overlayProducedAnalyticsEvent eventSnapshot $eventSnapshot"
            )
        }

        //optional
        override fun userState(userSnapshot: UserSnapshot) {
            Log.d(this.javaClass.simpleName, "overlayUserUpdated userSnapshot $userSnapshot")
        }

        override fun overlayRequestedPlay() {
            shouldNotShowControls = true
            super.overlayRequestedPlay()
        }

        override fun overlayRequestedPause() {
            shouldNotShowControls = true
            super.overlayRequestedPlay()
        }

        override fun overlayRequestedSeekTo(timestampMillis: Long) {
            shouldNotShowControls = true
            super.overlayRequestedSeekTo(timestampMillis)
        }

        override fun overlayRequestedFocus(focusView: View) {
            Log.d(this.javaClass.simpleName, "overlayRequestedFocus focusView=$focusView")
        }

        override fun overlayReleasedFocus(popMessage: Boolean) {
            Log.d(this.javaClass.simpleName, "overlayReleasedFocus popMessage=$popMessage")
        }

        override fun overlayDidShowSidebar() {}

        override fun overlayDidHideSidebar() {}

        override fun overlayClickedUserArea() {
            Log.d("ITG", "CLICKED USER AREA")
        }

        override fun overlayClosedByUser(type: CloseOption, timestamp: Long) {
            Log.d("ITG", "ITG CLOSED - ${type.name}")
        }

    }

    companion object {
        private const val UPDATE_DELAY = 16
    }
}
