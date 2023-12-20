package com.syncedapps.inthegametvexample

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import java.util.*

import com.syncedapps.inthegametv.integration.ITGKalturaPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametv.domain.model.Storage
import com.syncedapps.inthegametv.domain.model.UserRole
import com.syncedapps.inthegametv.network.ITGEnvironment
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent
import android.annotation.SuppressLint

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var playbackPosition: Long = 0L
    private var currentItem: Int = 0
    private var playWhenReady: Boolean = true

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGKalturaPlayerAdapter? = null

    companion object {
        private  val TAG = PlaybackPhoneActivity::class.java.simpleName

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "653647b68b8364785c095ae3"
        val channelSlug = "espn"


        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(this)


        // Set up the ITGKalturaPlayerAdapter
        val adapter = ITGKalturaPlayerAdapter()
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            activity = this, //mandatory: fragment activity instance
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component


            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
            extraDataSlug = null, //optional: secondary channel or category
            userBroadcasterForeignID = null, //optional: your user UUID
            userInitialName = null, //optional: viewer's name/nickname
            userRole = UserRole.USER, //optional: 'user', 'guest'
            userInitialAvatarUrl = null, //optional: viewer's avatar absolute url
            userEmail = null, //optional: viewer's email
            userPhone = null, //optional: viewer's phone
            language = null, //optional: 'en', 'es', 'he', 'ru'
            itgEnvironment = ITGEnvironment.v2_3, //optional: ITG stable environment route
            storage = Storage.CDN, //optional: 'Storage.CDN', 'Storage.BLOB'
            webp = false //optional: use webp equivalents for images added via admin panel
        )


        // Add the ITG component to your view hierarchy
        binding.itgContainer.addView(mITGComponent, 0)

        initializePlayer()
        initUi()

        prepareMediaEntry(Const.VIDEO_URL)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                    // Implement your own back press action here
                    finish()
                }
            }
        })

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


    private fun restorePlaybackStateIfAny(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt("currentItem")
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }
    }

    private fun setupFullscreenMode() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun initializePlayer() {
        val playerInitOptions = PlayerInitOptions()
        val  player = KalturaBasicPlayer.create(this, playerInitOptions)
        player.setPlayerView(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        this.player = player
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentItem", currentItem)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)

        // Saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }

    private var player: KalturaPlayer? = null
    private var nowPlaying: Boolean = false
    private var firstLaunch = true
    private var adCuePoints: AdCuePoints? = null

    // KalturaPlayer
    private fun prepareMediaEntry(videoUrl : String?) {
        val pkMediaEntry = createMediaEntry(videoUrl)
        player?.setMedia(pkMediaEntry)
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(videoUrl : String?): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = "1_w9zx2eti"
        mediaEntry.duration = (883 * 1000).toLong()
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createMediaSources(videoUrl)

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createMediaSources(videoUrl : String?): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = "11111"
        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = videoUrl
        mediaSource.mediaFormat = PKMediaFormat.valueOfUrl(videoUrl)


        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    private fun initUi() {
        binding.replay.setOnClickListener {
            if (player != null) {
                player?.replay()
            }
            binding.replay.visibility = View.GONE
        }

        binding.playerControls.setPlayer(player)
        addPlayerListeners(binding.progressBarSpinner)
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    override fun onDestroy() {
        @Suppress("SameParameterValue")
        if (player != null) {
            player?.removeListeners(this)
            player?.destroy()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

            player = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (firstLaunch) {
            firstLaunch = false
            return
        }
        if (player != null) {
            player?.onApplicationResumed()
        }
    }

    private fun addPlayerListeners(appProgressBar: ProgressBar) {
        player?.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log("AD_PLAYBACK_INFO_UPDATED")
            //log.d("XXX playbackInfoUpdated  = " + playbackInfoUpdated.width + "/" + playbackInfoUpdated.height + "/" + playbackInfoUpdated.bitrate);
            log("AD_PLAYBACK_INFO_UPDATED bitrate = " + event.bitrate)
        }

        player?.addListener(
            this,
            AdEvent.skippableStateChanged
        ) { log("SKIPPABLE_STATE_CHANGED") }

        player?.addListener(this, AdEvent.adRequested) {
            log("AD_REQUESTED")// adtag = " + adRequestEvent.adTagUrl);
        }

        player?.addListener(this, AdEvent.playHeadChanged) {
            appProgressBar.visibility = View.INVISIBLE
            //log.d("received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }

        player?.addListener(this, AdEvent.error) { event ->
            Log.d(TAG, "AD_ERROR " + event.type + " " + event.error.message)
            appProgressBar.visibility = View.INVISIBLE
            binding.playerControls.setSeekBarStateForAd(false)
            log("AD_ERROR")
        }

        player?.addListener(this, AdEvent.adBreakStarted) {
            log("AD_BREAK_STARTED")
            appProgressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            Log.d(TAG, "cuepointsChanged. Has Postroll  = " + event.cuePoints.hasPostRoll())
            adCuePoints = event.cuePoints
            if (adCuePoints != null) {
                Log.d(TAG, "Has Post roll = " + adCuePoints?.hasPostRoll())
            }

            log("AD_CUEPOINTS_UPDATED")
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log("AD_LOADED " + event.adInfo.adIndexInPod + "/" + event.adInfo.totalAdsInPod)
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.started) { event ->
            log("AD_STARTED w/h - " + event.adInfo.adWidth + "/" + event.adInfo.adHeight)
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.resumed) {
            log("AD_RESUMED")
            nowPlaying = true
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.paused) {
            log("AD_PAUSED")
            nowPlaying = true
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null && adController.isAdDisplayed) {
                    log("Ad " + adController.adCurrentPosition + "/" + adController.adDuration)
                } else {
                    log("Player " + it.currentPosition + "/" + it.duration)
                }
            }
        }

        player?.addListener(this, AdEvent.skipped) { log("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) {
            log("AD_ALL_ADS_COMPLETED")
            val hasPostRoll = adCuePoints?.hasPostRoll() ?: false
            if (adCuePoints != null && hasPostRoll) {
                binding.replay.visibility = View.VISIBLE
            }

            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.completed) {
            log("AD_COMPLETED")
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.firstQuartile) { log("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) {
            log("MIDPOINT")
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        log(adController.adCurrentPosition.toString() + "/" + adController.adDuration)
                    }
                }
            }
        }

        player?.addListener(this, AdEvent.thirdQuartile) { log("THIRD_QUARTILE") }

        player?.addListener(this, AdEvent.adBreakEnded) { log("AD_BREAK_ENDED") }

        player?.addListener(this, AdEvent.adClickedEvent) { event ->
            log("AD_CLICKED")
            Log.d(TAG, "AD_CLICKED url = " + event.clickThruUrl)
        }

        player?.addListener(this, AdEvent.adBufferStart) {
            log("AD_STARTED_BUFFERING")
            appProgressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.adBufferEnd) {
            log("AD_BUFFER_END")
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.adBreakEnded) {
            log("AD_BREAK_ENDED")
            appProgressBar.visibility = View.INVISIBLE
        }

        ////PLAYER Events
        player?.addListener(this, PlayerEvent.videoFramesDropped) {
            //log("VIDEO_FRAMES_DROPPED " + videoFramesDropped.droppedVideoFrames);
        }

        player?.addListener(this, PlayerEvent.bytesLoaded) {
            //log("BYTES_LOADED " + bytesLoaded.bytesLoaded);
        }

        player?.addListener(this, PlayerEvent.play) {
            log("PLAYER PLAY")
            nowPlaying = true
        }

        player?.addListener(this, PlayerEvent.pause) {
            log("PLAYER PAUSE")
            nowPlaying = false
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null && adController.isAdDisplayed) {
                    log("Ad " + adController.adCurrentPosition + "/" + adController.adDuration)
                } else {
                    log("Player " + it.currentPosition + "/" + it.duration)
                }
            }
        }

        player?.addListener(this, PlayerEvent.error) { event ->
            log("PLAYER ERROR " + event.error.message!!)
            appProgressBar.visibility = View.INVISIBLE
            nowPlaying = false
        }

        player?.addListener(this, PlayerEvent.ended) {
            log("PLAYER ENDED")
            appProgressBar.visibility = View.INVISIBLE
            val hasPostRoll = adCuePoints?.hasPostRoll() ?: false
            if (adCuePoints == null || !hasPostRoll) {
                binding.replay.visibility = View.VISIBLE
            }
            nowPlaying = false
        }

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            //log("State changed from " + stateChanged.oldState + " to " + stateChanged.newState);
            if (event.newState == PlayerState.BUFFERING) {
                appProgressBar.visibility = View.VISIBLE

            } else if (event.newState == PlayerState.READY) {
                appProgressBar.visibility = View.INVISIBLE
            }
            binding.playerControls.setPlayerState(event.newState)
            if (event.newState == PlayerState.READY && player?.isPlaying == true) {
                binding.playerControls.setIsPlaying(true)
            } else if (event.newState != PlayerState.READY) {
                binding.playerControls.setIsPlaying(false)
            } else {
                //do nothing
            }
        }

        player?.addListener(this, PlayerEvent.play) {
            binding.playerControls.setIsPlaying(true)
        }

        player?.addListener(this, PlayerEvent.pause) {
            binding.playerControls.setIsPlaying(false)
        }

        player?.playerView?.setOnClickListener {
            binding.playerControls.switchVisibility()
        }
    }

    private fun log(message: String) {
        Log.d(this.javaClass.simpleName, message)
    }

}
