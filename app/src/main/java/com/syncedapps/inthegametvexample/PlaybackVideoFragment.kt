package com.syncedapps.inthegametvexample

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.syncedapps.inthegametv.ITGAnimationType
import com.syncedapps.inthegametv.ITGOverlayView
import com.syncedapps.inthegametv.data.CloseOption
import com.syncedapps.inthegametv.interaction.*
import com.syncedapps.inthegametv.network.ITGEnvironment
import com.syncedapps.inthegametvexample.customViews.*
import kotlin.math.roundToInt


/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment(), ITGOverlayView.ITGOverlayListener, ITGOverlayView.ITGLayoutListener, VideoPlayerGlue.OnActionClickedListener, Player.Listener {


    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
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
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        //specify the environment - with custom values if needed
        val environment = ITGEnvironment.test

        //create the overlay
        val overlay = ITGOverlayView(requireContext())
        //load your channel to start up the ITG system
        overlay.load("testchannel", "testings", environment)
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
        mOverlay?.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
        }
        removeKeyInterceptor()
    }

    /** Pauses the player.  */
    @TargetApi(Build.VERSION_CODES.N)
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
        mOverlay?.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroyView() {
        mOverlay?.onDestroyView()
        super.onDestroyView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mOverlay?.updateOrientation()
    }


    private fun initializePlayer() {
        val player =  ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player
        mPlayerAdapter = LeanbackPlayerAdapter(requireContext(), player, UPDATE_DELAY)
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, this)
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true
        mPlayer?.addListener(this)
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
        mPlayerGlue?.title = movie.title
        mPlayerGlue?.subtitle = movie.description
        prepareMediaForPlaying(Uri.parse(movie.videoUrl))
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
    fun handleBackPressIfNeeded() : Boolean {
        return mOverlay?.handleBackPressIfNeeded() ?: false
    }

    fun receivedKeyEvent(event: KeyEvent) {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (isControlsOverlayVisible) {
                hideControlsOverlay(true)
            } else if(mOverlay?.isDisplayingInteraction() == false
                && mOverlay?.isSidebarVisible() == false) {
                showControlsOverlay(true)
                if (mOverlay?.isMenuVisible() == true) {
                    mOverlay?.hideMenu()
                }
            }
        }
        if (isControlsOverlayVisible && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) return
        mOverlay?.receivedKeyEvent(event)
    }

    private fun removeKeyInterceptor() {
        val rowsSupportFragment = childFragmentManager.findFragmentById(
            androidx.leanback.R.id.playback_controls_dock
        ) as RowsSupportFragment?
        rowsSupportFragment?.verticalGridView?.setOnKeyInterceptListener { false }
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
    override fun overlayRequestedFocus(focusView: View) {}

    //overlay finished showing content
    //if needed you can use this method to focus on your content
    override fun overlayReleasedFocus(popMessage: Boolean) {}

    override fun overlayResizeVideoHeight(activityHeight: Float) {
        if (this.isDetached || context == null) return

        val total = requireView().height.toFloat()
        val scale = (total - activityHeight) / total
        surfaceView.animate().scaleY(scale)
        surfaceView.animate().translationY(-activityHeight / 2)
    }

    override fun overlayResetVideoHeight() {
        if (this.isDetached || context == null) return
        surfaceView.animate().scaleY(1f)
        surfaceView.animate().translationY(0f)
    }

    override fun overlayResizeVideoWidth(activityWidth: Float) {
        val overlay = mOverlay ?: return
        val rtl = overlay.isRTLEnabled()

        val total = requireView().width.toFloat()
        val scale = (total - activityWidth) / total
        surfaceView.animate().scaleX(scale)
        surfaceView.animate().translationX((if(rtl) activityWidth else -activityWidth) / 2)
    }

    override fun overlayResetVideoWidth() {
        surfaceView.animate().scaleX(1f)
        surfaceView.animate().translationX(0f)
    }

    override fun overlayRequestedVideoTime() {
        val time = mPlayer?.currentPosition ?: 0
        mOverlay?.videoPlaying(time)
    }

    override fun overlayClickedUserArea() {
        Log.d("ITG", "Clicked user area")
    }

    override fun overlayClosedByUser(type: CloseOption, timestamp: Long) {
        //called when user closes the ITG service for a period of time
    }

    override fun overlayDidShowSidebar() {
        val spacing = convertDpToPixel(requireContext(), 192).toFloat()
        val total = requireView().width.toFloat()
        val scale = (total - spacing) / total
        surfaceView.animate().scaleX(scale)
        surfaceView.animate().translationX(-spacing / 2)
    }

    override fun overlayDidHideSidebar() {
        surfaceView.animate().scaleX(1f)
        surfaceView.animate().translationX(0f)
    }

    override fun overlayDidTapVideo() {}


    override fun overlayRequestedPortraitTopGap(): Int {
        //for mobile phones only
        //we can return 0 on TV
        return 0
    }

    @Suppress("SameParameterValue")
    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
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
    @Suppress("RedundantNullableReturnType")
    override fun customPollView(): ITGPollView? {
        return CustomPollView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customRatingView(): ITGRatingView? {
        return CustomRatingView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customTriviaView(): ITGTriviaView? {
        return CustomTriviaView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customWikiView(): ITGWikiView? {
        return CustomWikiView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customNoticeView(): ITGNotice? {
        return CustomNoticeView(requireContext())
    }

    @Suppress("RedundantNullableReturnType")
    override fun customProductView(): ITGProductView? {
        return CustomProductView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customCloseOptionsView(): ITGCloseOptionsView? {
        return CustomCloseOptionsView(context)
    }

    @Suppress("RedundantNullableReturnType")
    override fun customNoticeWikiView(): ITGNoticeWiki? {
        return ITGNoticeWiki(requireContext())
    }

    //ExoPlayer events
    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        Log.i("PLAYER", "On seek")
        val time = mPlayer?.currentPosition ?: 0
        mOverlay?.videoPlaying(time)
    }

    companion object {
        private const val UPDATE_DELAY = 16
    }
}
