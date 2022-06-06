package com.syncedapps.inthegametvexample

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.MediaController
import com.syncedapps.inthegametv.*
import com.syncedapps.inthegametv.interaction.*
import com.syncedapps.inthegametv.network.CloseOption
import com.syncedapps.inthegametv.network.ITGEnvironment
import com.syncedapps.inthegametvdemo.CustomViews.CustomProductView
import com.syncedapps.inthegametvexample.CustomViews.*
import kotlinx.android.synthetic.main.activity_phone_playback.*
import java.net.URI

class PlaybackPhoneActivity: Activity(), ITGOverlayView.ITGOverlayListener, ITGOverlayView.ITGLayoutListener {
    private var mediaController: MediaController? = null
    private var mOverlay: ITGOverlayView? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_playback)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val settings = ITGSettings(this)
        settings.clearUserToken()

        startVideo()
        addOverlay()
    }

    fun startVideo() {
        val list = MovieList.list
        val movie = list.first()

        videoView.setVideoPath(movie.videoUrl)
        videoView.requestFocus()
        videoView.start()

        videoView.setOnPreparedListener { mp: MediaPlayer ->
            mediaPlayer = mp
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.setVolume(1f, 1f)

            mp.setOnSeekCompleteListener { mp ->
                val time = mp.currentPosition.toLong()
                mOverlay?.videoPlaying(time)
            }
        }

        videoView.setOnPreparedListener {
            it.setOnVideoSizeChangedListener { _, _, _ ->
                if (mediaController == null) {
                    mediaController = MediaController(this)
                    videoView.setMediaController(mediaController!!)
                }
                mediaController?.setAnchorView(outerContainer)
            }
        }
    }

    fun addOverlay() {
        //specify the environment - with custom values if needed
        val environment = ITGEnvironment.devDefault

        val overlay = ITGOverlayView(this)
        //load your channel to start up the ITG system
        overlay.load("soccer_predictions", "demos", environment)
        overlay.listener = this
        overlay.showInPortrait(true)

        // enable the layout delegate if you wish to set custom layouts
//        overlay.layoutListener = this
        //use this method if you wish to show content when phone is on portrait mode
//        overlay.showInPortrait(true)
        // you can adjust the spacing between the content and bottom of the screen
//        overlay.setBottomPaddingDp(30)
        // use this optional variable to set the animation type
//        overlay.animationType = ITGAnimationType.FROM_RIGHT

        // use this variable if you want to hide the win notifications
//        overlay.showNotices = false
        // use this if you want notifications to display on the bottom area like regular activities
//        overlay.showNoticeAsActivity = true

        //add the overlay to your view hierarchy
        container.addView(overlay)
        mOverlay = overlay

        //we advise you to use your video players' playback listener
        //to inform the overlay of the video status (playing/paused and time),
        //just like in the TV example.
        //the implementation will depend on what video player you use
    }

    override fun onBackPressed() {
        if (mOverlay?.handleBackPressIfNeeded() == true) {
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun overlayRequestedPause() {
    }

    override fun overlayRequestedPlay() {
    }

    override fun overlayRequestedFocus(focusView: View) {}

    override fun overlayReleasedFocus(popMessage: Boolean) {}

    override fun overlayResizeVideoHeight(activityHeight: Float) {
        val pixelSpacing = if (isPortrait()) 108 else 86
        val spacing = convertDpToPixel(this, pixelSpacing).toFloat()
        val total = container!!.height.toFloat()
        val scale = (total - spacing) / total
        videoView.animate().scaleY(scale)
        videoView.animate().translationY(-spacing / 2)
    }

    override fun overlayResetVideoHeight() {
        videoView.animate().scaleY(1f)
        videoView.animate().translationY(0f)
    }

    override fun overlayResizeVideoWidth(activityWidth: Float) {}
    override fun overlayResetVideoWidth() {}

    override fun overlayRequestedVideoTime() {
        val time = mediaPlayer?.currentPosition?.toLong() ?: 0
        mOverlay?.videoPlaying(time)
    }

    override fun overlayClickedUserArea() {
    }

    override fun overlayClosedByUser(type: CloseOption, timestamp: Long) {
    }

    override fun overlayDidShowSidebar() {
    }

    override fun overlayDidHideSidebar() {
    }

    override fun overlayDidTapVideo() {
        if (mediaController?.isShowing == true) {
            mediaController?.hide()
        } else {
            mediaController?.show(0)
            Handler().postDelayed({
                if (mediaController?.isShowing == true) {
                    mediaController?.hide()
                }
            }, 2000)
        }
    }

    override fun overlayRequestedPortraitTopGap(): Int {
        return videoView?.top ?: 0
    }

    override fun onStart() {
        super.onStart()
        mOverlay?.onStart()
    }

    override fun onStop() {
        mOverlay?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mOverlay?.onDestroyView()
        super.onDestroy()
    }

    //the layout methods are optional
    //use them only if you want to customize the design elements

    override fun customPollView(): ITGPollView? {
        return CustomPollView(this)
    }

    override fun customRatingView(): ITGRatingView? {
        return CustomRatingView(this)
    }

    override fun customTriviaView(): ITGTriviaView? {
        return CustomTriviaView(this)
    }

    override fun customWikiView(): ITGWikiView? {
        return CustomWikiView(this)
    }

    override fun customNoticeView(): ITGNotice? {
        return CustomNoticeView(this)
    }

    override fun customProductView(): ITGProductView? {
        return CustomProductView(this)
    }

    override fun customCloseOptionsView(): ITGCloseOptionsView? {
        return CustomCloseOptionsView(this)
    }

    override fun customNoticeWikiView(): ITGNoticeWiki? {
        return ITGNoticeWiki(this)
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun isPortrait(): Boolean {
        val orientation = resources.configuration.orientation
        return (orientation == Configuration.ORIENTATION_PORTRAIT)
    }
}