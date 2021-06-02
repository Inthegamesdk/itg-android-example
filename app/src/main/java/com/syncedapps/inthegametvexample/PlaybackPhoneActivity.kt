package com.syncedapps.inthegametvexample

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.syncedapps.inthegametv.*
import com.syncedapps.inthegametvexample.CustomViews.CustomNoticeView
import com.syncedapps.inthegametvexample.CustomViews.CustomRatingView
import com.syncedapps.inthegametvexample.CustomViews.CustomTriviaView
import com.syncedapps.inthegametvexample.CustomViews.CustomWikiView
import kotlinx.android.synthetic.main.activity_phone_playback.*
import java.net.URI

class PlaybackPhoneActivity: Activity(), ITGOverlayView.ITGOverlayListener, ITGOverlayView.ITGLayoutListener {
    private var mediaController: MediaController? = null
    private var mOverlay: ITGOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_playback)

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
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.setVolume(1f, 1f)
        }

        mediaController = MediaController(this)
        videoView.setMediaController(mediaController!!)
    }

    fun addOverlay() {
        val overlay = ITGOverlayView(this)
        //load your channel to start up the ITG system
        overlay.load("ORLvsNYCFC", "orlandofcchannel")
        overlay.listener = this

        // enable the layout delegate if you wish to set custom layouts
//        overlay.layoutListener = this
        //use this variable if you wish to hide all content when phone is on portrait mode
//        overlay.hideInPortraitMode = true
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

    override fun overlayRequestedFocus(focusView: View) {
    }

    override fun overlayReleasedFocus(popMessage: Boolean) {
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
}