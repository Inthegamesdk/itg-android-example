package com.syncedapps.inthegametvexample

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.syncedapps.inthegametv.ITGContent
import com.syncedapps.inthegametv.ITGSettings
import com.syncedapps.inthegametv.domain.model.AnalyticsEventSnapshot
import com.syncedapps.inthegametv.domain.model.UserSnapshot
import com.syncedapps.inthegametv.integration.ITGExoPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import java.util.*

class PlaybackPhoneActivity : FragmentActivity() {

    private var mITGExoPlayerAdapter: ITGExoPlayerAdapter? = null
    private var mITGComponent: PhoneITGComponent? = null
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

        //test purpose only
        ITGSettings(this).clearAll()

        startVideo()
        addOverlay(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mITGComponent?.handleBackPressIfNeeded() == false) {
                    // Back is pressed... Finishing the activity
                    finish()
                }
            }
        })
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

    private fun buildVideoView(): StyledPlayerView {
        return layoutInflater.inflate(R.layout.styled_player_view, null, false) as StyledPlayerView
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT)
            .setSeekForwardIncrementMs(SEEK_INCREMENT)
            .build()
            .also { exoPlayer ->
                mITGExoPlayerAdapter?.onPlayerReady(exoPlayer)
            }
    }

    private fun releasePlayer() {
        Log.d(this.javaClass.simpleName, "releasePlayer")
        player?.let { exoPlayer ->
            mITGExoPlayerAdapter?.onPlayerReleased()
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun addOverlay(savedInstanceState: Bundle?) {
        // load your channel to start up the ITG system
        val adapter = ITGExoPlayerAdapter(
            playerView = buildVideoView()
        )
        mITGExoPlayerAdapter = adapter
        val itgComponent = PhoneITGComponent(this)
        mITGComponent = itgComponent
        itgComponent.init(
            this,
            adapter,
            Const.environment,
            Const.ACCOUNT_ID,
            Const.CHANNEL_SLUG,
            language = Const.LANGUAGE,
            userBroadcasterForeignID = "android_${Date().time}",
            savedState = savedInstanceState
        )
        binding.outerContainer.addView(mITGComponent)
    }

    override fun onResume() {
        super.onResume()
        if ((Build.VERSION.SDK_INT <= 23 || player == null)) {
            initializePlayer()
            startVideo()
        }
    }

    override fun onStop() {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mITGComponent?.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)
    }

    inner class PhoneITGComponent : ITGPlaybackComponent {
        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

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
                    Toast.makeText(this@PlaybackPhoneActivity, customUrl, Toast.LENGTH_LONG).show()
                }
            }
        }

        //  optional
        override fun overlayProducedAnalyticsEvent(eventSnapshot: AnalyticsEventSnapshot) {
            Log.d(
                this.javaClass.simpleName,
                "overlayProducedAnalyticsEvent eventSnapshot $eventSnapshot"
            )
        }

        override fun userState(userSnapshot: UserSnapshot) {
            Log.d(this.javaClass.simpleName, "overlayUserUpdated userSnapshot $userSnapshot")
        }

        override fun overlayDidPresentContent(content: ITGContent) {
            Log.d(this.javaClass.simpleName, "overlayDidPresentContent content $content")
        }

        override fun overlayDidEndPresentingContent(content: ITGContent) {
            Log.d(this.javaClass.simpleName, "overlayDidEndPresentingContent content $content")
        }

        override fun overlayDidTapVideo() {
            val videoView = mITGExoPlayerAdapter?.getVideoView() as? StyledPlayerView ?: return
            if (videoView.isControllerFullyVisible) {
                videoView.hideController()
            } else {
                videoView.showController()
                controlsTimestamp = System.currentTimeMillis()
                Handler(Looper.getMainLooper()).postDelayed({
                    val time = System.currentTimeMillis()
                    if (videoView.isControllerFullyVisible && time - controlsTimestamp > 3900) {
                        videoView.hideController()
                    }
                }, 4000)
            }
        }

    }

    companion object {
        private const val SEEK_INCREMENT = 10_000L
    }
}