package com.syncedapps.inthegametvexample

import android.annotation.TargetApi
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.CursorObjectAdapter
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.syncedapps.inthegametv.*
import com.syncedapps.inthegametvexample.CustomViews.CustomRatingView
import com.syncedapps.inthegametvexample.CustomViews.CustomTriviaView


/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment(), ITGOverlayView.ITGOverlayListener, ITGOverlayView.ITGLayoutListener, VideoPlayerGlue.OnActionClickedListener {

    private val UPDATE_DELAY = 16

    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: SimpleExoPlayer? = null
    private var mTrackSelector: TrackSelector? = null
    private var mMovie: Movie? = null
    private var mOverlay: ITGOverlayView? = null
    private var shouldNotShowControls = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMovie =
            activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //create the overlay
        val overlay = ITGOverlayView(context)
        //load your channel to start up the ITG system
        overlay.load("ORLvsNYCFC", "orlandofcchannel")
        overlay.listener = this

        // enable the layout delegate if you wish to set custom layouts
        overlay.layoutListener = this
        // you can adjust the spacing between the content and bottom of the screen
        overlay.setBottomPaddingDp(30)
        // use this optional variable to set the animation type
        overlay.animationType = ITGAnimationType.FROM_RIGHT

        //add the overlay to your view hierarchy
        (view as ViewGroup).addView(overlay)
        mOverlay = overlay
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
    @TargetApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue?.isPlaying() == true) {
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

    private fun initializePlayer() {
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        mTrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        mPlayer = ExoPlayerFactory.newSimpleInstance(activity, mTrackSelector)
        mPlayerAdapter = LeanbackPlayerAdapter(activity, mPlayer, UPDATE_DELAY)
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, this)
        mPlayerGlue?.setHost(VideoSupportFragmentGlueHost(this))
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true
        play(mMovie)

    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer?.release()
            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    private fun play(movie: Movie?) {
        if (movie == null) return
        mPlayerGlue?.setTitle(movie.title)
        mPlayerGlue?.setSubtitle(movie.description)
        prepareMediaForPlaying(Uri.parse(movie.videoUrl))
        mPlayerGlue?.play()
    }

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val userAgent: String = Util.getUserAgent(activity, "VideoPlayerGlue")
        val mediaSource: MediaSource = ExtractorMediaSource(
            mediaSourceUri,
            DefaultDataSourceFactory(activity, userAgent),
            DefaultExtractorsFactory(),
            null,
            null
        )
        mPlayer?.prepare(mediaSource)
    }

    // let the overlay handle the back button press if it needs to
    // (to dismiss interactions)
    fun handleBackPressIfNeeded() : Boolean {
        return mOverlay?.handleBackPressIfNeeded() ?: false
    }

    // overlay will request the video to play/pause for some interactions
    override fun overlayRequestedPause() {
        shouldNotShowControls = true
        mPlayerGlue?.pause()
    }

    override fun overlayRequestedPlay() {
        shouldNotShowControls = true
        mPlayerGlue?.play()
    }

    override fun overlayRequestedFocus(focusView: View) {
        //will be called when the overlay shows content
        //use this method to send focus to the overlay if needed
        //focusView is the element that should become focused
    }

    override fun overlayReleasedFocus() {
        //overlay finished showing content
        //if needed you can use this method to focus on your content
    }

    override fun showControlsOverlay(runAnimation: Boolean) {
        if (shouldNotShowControls) {
            shouldNotShowControls = false
        } else {
            super.showControlsOverlay(runAnimation)
        }
    }

    //pass play/pause events to overlay so that it can track the video time
    override fun onPlayAction() {
        val time = mPlayer?.currentPosition ?: 0
        mOverlay?.videoPlaying(time)
    }

    override fun onPauseAction() {
        mOverlay?.videoPaused()
    }

    override fun onPrevious() { }

    override fun onNext() { }

    //the layout methods are optional
    //use them only if you want to customize the design elements

    override fun customPollView(): ITGPollView? {
//        return CustomPollView(context)
        return null
    }

    override fun customRatingView(): ITGRatingView? {
//        return CustomRatingView(context)
        return null
    }

    override fun customTriviaView(): ITGTriviaView? {
//        return CustomTriviaView(context)
        return null
    }
}
