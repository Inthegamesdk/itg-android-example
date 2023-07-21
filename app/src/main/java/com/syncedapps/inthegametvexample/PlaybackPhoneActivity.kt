package com.syncedapps.inthegametvexample

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.syncedapps.inthegametv.ITGOverlayView
import com.syncedapps.inthegametv.ITGSettings
import com.syncedapps.inthegametv.data.CloseOption
import com.syncedapps.inthegametv.domain.model.AnalyticsEventSnapshot
import com.syncedapps.inthegametv.domain.model.UserSnapshot
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import kotlin.math.roundToInt

class PlaybackPhoneActivity : FragmentActivity(),
    ITGOverlayView.ITGOverlayListener,
    Player.Listener {

    private var mOverlay: ITGOverlayView? = null
    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var itgState: Bundle? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private var controlsTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState != null) {
            itgState = savedInstanceState.getBundle("itgState")
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }

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

        //observe controls
        binding.videoView.setControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE) {
                mOverlay?.openMenu()
            } else {
                mOverlay?.hideMenu()
            }
        }

        //test purpose only
        ITGSettings(this).clearAll()

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape) {
            binding.videoView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = 0.5f
                dimensionRatio = null
            }
            binding.container.updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = 0.5f
                topToTop = ConstraintSet.PARENT_ID
                topToBottom = ConstraintSet.UNSET
            }
            Handler(Looper.getMainLooper()).post {
                if ( binding.videoView.isControllerVisible)
                    mOverlay?.openMenu()
                else
                    mOverlay?.hideMenu()
            }
        } else {
            binding.container.updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = 0.5f
                topToTop = ConstraintSet.UNSET
                topToBottom = R.id.videoView
            }
            binding.videoView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                verticalBias = 0f
                dimensionRatio = "16:9"
            }
        }
    }

    private fun startVideo() {
        val list = MovieList.list
        val movie = list.first()

        prepareMediaForPlaying(Uri.parse(movie.videoUrl))
        player?.playWhenReady = playWhenReady
        player?.seekTo(0, playbackPosition)
        player?.prepare()
    }

    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(this, upstreamDataSourceFactory)

        defaultDataSourceFactory.createDataSource()

        val mediaSource: MediaSource =
            if (mediaSourceUri.lastPathSegment?.endsWith(".m3u8") == true) {
                HlsMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        com.google.android.exoplayer2.MediaItem.fromUri(
                            mediaSourceUri
                        )
                    )
            } else {
                ProgressiveMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        com.google.android.exoplayer2.MediaItem.fromUri(
                            mediaSourceUri
                        )
                    )
            }
        player?.setMediaSource(mediaSource)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT)
            .setSeekForwardIncrementMs(SEEK_INCREMENT)
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(this)
                binding.videoView.player = exoPlayer
            }
    }

    private fun releasePlayer() {
        Log.d(this.javaClass.simpleName, "releasePlayer")
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(this)
            exoPlayer.release()
        }
        player = null
    }

    private fun addOverlay() {
        //specify the environment - with custom values if needed
        //create the overlay
        val overlay = ITGOverlayView(this, Const.environment)
        //load your channel to start up the ITG system
        overlay.load(Const.ACCOUNT_ID, Const.CHANNEL_SLUG, Const.LANGUAGE, savedState = itgState)
        overlay.listener = this
        //enable portrait support if it's needed
        overlay.showInPortrait(true)

        //add the overlay to your view hierarchy
        binding.container.addView(overlay)
        mOverlay = overlay

        //we advise you to use your video players' playback listener
        //to inform the overlay of the video status (playing/paused and time),
        //just like in the TV example.
        //the implementation will depend on what video player you use
    }

    override fun overlayRequestedPause() {
        try {
            player?.pause()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun overlayRequestedPlay() {
        try {
            player?.play()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun overlayRequestedSeekTo(timestampMillis: Long) {
        try {
            player?.seekTo(timestampMillis)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
    override fun overlayRequestedFocus(focusView: View) {}

    override fun overlayReleasedFocus(popMessage: Boolean) {}

    override fun overlayResizeVideoHeight(activityHeight: Float) {
        val pixelSpacing = activityHeight.roundToInt()
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
    override fun userState(userSnapshot: UserSnapshot) {
        Log.d(this.javaClass.simpleName, "userState $userSnapshot")
    }

    override fun overlayResetVideoWidth() {}

    override fun overlayRequestedVideoTime() {
        player?.let { player ->
            val state = player.playbackState
            if (state == Player.STATE_READY && player.isPlaying) {
                mOverlay?.videoPlaying(player.currentPosition)
            } else {
                mOverlay?.videoPaused(player.currentPosition) //let's pause any interactions
            }
        }
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
        if (binding.videoView.isControllerVisible) {
            binding.videoView.hideController()
        } else {
            binding.videoView.showController()
            controlsTimestamp = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                val time = System.currentTimeMillis()
                if (binding.videoView.isControllerVisible && time - controlsTimestamp > 3900) {
                    binding.videoView.hideController()
                }
            }, 4000)
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

    override fun overlayProducedAnalyticsEvent(eventSnapshot: AnalyticsEventSnapshot) {
        Log.d(this.javaClass.simpleName, "overlayProducedAnalyticsEvent eventSnapshot $eventSnapshot")
    }

    override fun onStart() {
        super.onStart()
        mOverlay?.onStart()
    }

    override fun onResume() {
        super.onResume()
        if ((Build.VERSION.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            startVideo()
        }
    }

    override fun onStop() {
        mOverlay?.onStop()
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

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    // ExoPlayer events
    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        val time = player?.currentPosition ?: 0
        Log.d(this.javaClass.simpleName, "onPositionDiscontinuity time=$time")
        mOverlay?.videoSought(time)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        Log.d(this.javaClass.simpleName, "onPlaybackStateChanged playbackState=$playbackState")
        val player = player ?: return
        /** inform the overlay when the player is ready to achieve the best clocks' precision **/
        if (playbackState == Player.STATE_READY && player.playWhenReady) {
            mOverlay?.videoPlaying(player.currentPosition)
        } else if (playbackState != Player.STATE_READY) {
            mOverlay?.videoPaused(player.currentPosition) //let's pause any scheduling
        } else {
            //do nothing
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        val player = player ?: return
        Log.d(this.javaClass.simpleName, "onIsPlayingChanged isPlaying=$isPlaying")
        if (isPlaying)
            mOverlay?.videoPlaying(player.currentPosition)
        else
            mOverlay?.videoPaused(player.currentPosition)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val itgState = mOverlay?.saveState()
        outState.putBundle("itgState", itgState)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)
    }

    companion object {
        private const val SEEK_INCREMENT = 10_000L
    }
}