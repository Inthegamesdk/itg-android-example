package com.syncedapps.inthegametvexample

import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import com.syncedapps.inthegametv.ITGOverlayView
import com.syncedapps.inthegametv.ITGSettings
import com.syncedapps.inthegametv.data.CloseOption
import com.syncedapps.inthegametv.network.ITGEnvironment
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

class PlaybackPhoneActivity : FragmentActivity(),
    ITGOverlayView.ITGOverlayListener,
    PlayStateBroadcastingVideoView.PlayPauseListener{

    private var mediaController: MediaController? = null
    private var mOverlay: ITGOverlayView? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var binding: ActivityPhonePlaybackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //test purpose only
        runBlocking {
            ITGSettings(
                this@PlaybackPhoneActivity,
                ITGEnvironment.stage
            ).clearAll()
        }

        startVideo()
        addOverlay()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mOverlay?.handleBackPressIfNeeded() == false) {
                    // Back is pressed... Finishing the activity
                    finish()
                }
            }
        })
    }

    private fun startVideo() {
        val list = MovieList.list
        val movie = list.first()

        binding.videoView.setPlayPauseListener(this)
        binding.videoView.setVideoPath(movie.videoUrl)
        binding.videoView.requestFocus()
        binding.videoView.start()

        binding.videoView.setOnPreparedListener { mp: MediaPlayer ->
            mediaPlayer = mp
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mp.setVolume(1f, 1f)

            mp.setOnSeekCompleteListener { _mp ->
                val time = _mp.currentPosition.toLong()
                mOverlay?.videoPlaying(time)
            }
        }

        binding.videoView.setOnPreparedListener {
            it.setOnVideoSizeChangedListener { _, _, _ ->
                if (mediaController == null) {
                    mediaController = MediaController(this)
                    binding.videoView.setMediaController(mediaController!!)
                }
                mediaController?.setAnchorView(binding.outerContainer)
            }
        }
    }

    private fun addOverlay() {
        //specify the environment - with custom values if needed
        val environment = ITGEnvironment.stage
        //create the overlay
        val overlay = ITGOverlayView(this, environment)
        //load your channel to start up the ITG system
        overlay.load("631da52247f9e460d1039022", "channel_one_stage", "HE")
        overlay.listener = this
        //enable portrait support if it's needed
        overlay.showInPortrait(true)
        // use this optional variable to set the animation type
//        overlay.animationType = ITGAnimationType.FROM_BOTTOM

        // use this if you want notifications to display on the bottom area like regular activities
//      overlay.showNoticeAsActivity = true

        //optional delay before showing injected activities
//        overlay.injectionDelay = 5
        //add the overlay to your view hierarchy
        binding.container.addView(overlay)
        mOverlay = overlay

        //we advise you to use your video players' playback listener
        //to inform the overlay of the video status (playing/paused and time),
        //just like in the TV example.
        //the implementation will depend on what video player you use
    }

    override fun overlayRequestedPause() {
    }

    override fun overlayRequestedPlay() {
    }

    override fun overlayRequestedSeekTo(timestampMillis: Long) {
        try {
            mediaPlayer?.seekTo(timestampMillis.toInt())
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun overlayRequestedFocus(focusView: View) {}

    override fun overlayReleasedFocus(popMessage: Boolean) {}

    override fun overlayResizeVideoHeight(activityHeight: Float) {
        val pixelSpacing = if (isPortrait()) 108 else 86
        val spacing = convertDpToPixel(this, pixelSpacing).toFloat()
        val total = binding.container.height.toFloat()
        val scale = (total - spacing) / total
        binding.videoView.animate().scaleY(scale)
        binding.videoView.animate().translationY(-spacing / 2)
    }

    override fun overlayResetVideoHeight() {
        binding.videoView.animate().scaleY(1f)
        binding.videoView.animate().translationY(0f)
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
            Handler(Looper.getMainLooper()).postDelayed({
                if (mediaController?.isShowing == true) {
                    mediaController?.hide()
                }
            }, 2000)
        }
    }

    override fun overlayReceivedDeeplink(customUrl: String) {
        //deeplink value could be specified on your own
        when (customUrl) {
            "next channel" -> {
                //TODO open next channel
            }
            "previous channel" -> {
                //TODO open prev channel
            }
            else -> {
                Toast.makeText(this, customUrl, Toast.LENGTH_LONG).show()
            }
        }
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
        binding.videoView.setPlayPauseListener(null)
        super.onDestroy()
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private fun isPortrait(): Boolean {
        val orientation = resources.configuration.orientation
        return (orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    override fun onPlayVideo() {
        val time = binding.videoView.currentPosition
        mOverlay?.videoPlaying(time.toLong())
    }

    override fun onPauseVideo() {
        val time = binding.videoView.currentPosition
        mOverlay?.videoPaused(time.toLong())
    }
}