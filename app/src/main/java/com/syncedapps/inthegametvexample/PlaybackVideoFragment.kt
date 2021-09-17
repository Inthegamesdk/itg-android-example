package com.syncedapps.inthegametvexample

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
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
import com.syncedapps.inthegametv.interaction.*
import com.syncedapps.inthegametv.network.ITGEnvironment
import com.syncedapps.inthegametvdemo.CustomViews.CustomProductView
import com.syncedapps.inthegametvexample.CustomViews.CustomNoticeView
import com.syncedapps.inthegametvexample.CustomViews.CustomRatingView
import com.syncedapps.inthegametvexample.CustomViews.CustomTriviaView
import com.syncedapps.inthegametvexample.CustomViews.CustomWikiView
import java.util.*
import kotlin.concurrent.schedule


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
        view.setBackgroundColor(resources.getColor(R.color.black))

        //specify the environment - with custom values if needed
        val environment = ITGEnvironment.testDefault

        //create the overlay
        val overlay = ITGOverlayView(context)
        //load your channel to start up the ITG system
        overlay.load("ORLvsNYCFC", "orlandofcchannel", environment)
        overlay.listener = this

        // enable the layout delegate if you wish to set custom layouts
//        overlay.layoutListener = this

        // you can adjust the spacing between the content and bottom of the screen
        overlay.setBottomPaddingDp(0)
        // use this optional variable to set the animation type
        overlay.animationType = ITGAnimationType.FROM_BOTTOM

        // use this variable if you want to hide the win notifications
//        overlay.showNotices = false

        // use this if you want notifications to display on the bottom area like regular activities
//        overlay.showNoticeAsActivity = true

        //it's possible to customize the auto-close time after answering
//        overlay.defaultAutoCloseTime = 10

        //optional delay before showing injected activities
//        overlay.injectionDelay = 5

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
        mOverlay?.shutdown()
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

    //will be called when the overlay shows content
    //use this method to send focus to the overlay if needed
    //focusView is the element that should become focused
    override fun overlayRequestedFocus(focusView: View) {
        val spacing = convertDpToPixel(requireContext(), 86).toFloat()
        val total = view!!.height.toFloat()
        val scale = (total - spacing) / total
        surfaceView.animate().scaleY(scale)
        surfaceView.animate().translationY(-spacing / 2)
    }

    //overlay finished showing content
    //if needed you can use this method to focus on your content
    override fun overlayReleasedFocus(popMessage: Boolean) {
        surfaceView.animate().scaleY(1f)
        surfaceView.animate().translationY(0f)

        if (popMessage) {
            Log.d("ITG", "Show account POPUP")
        } else {
            Log.d("ITG", "Do not show popup")
        }
    }

    override fun overlayClickedUserArea() {
        Log.d("ITG", "Clicked user area")
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
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
        return CustomPollView(context)
    }

    override fun customRatingView(): ITGRatingView? {
        return CustomRatingView(context)
    }

    override fun customTriviaView(): ITGTriviaView? {
        return CustomTriviaView(context)
    }

    override fun customWikiView(): ITGWikiView? {
        return CustomWikiView(context)
    }

    override fun customNoticeView(): ITGNotice? {
        return CustomNoticeView(context)
    }

    override fun customProductView(): ITGProductView? {
        return CustomProductView(context)
    }
}
