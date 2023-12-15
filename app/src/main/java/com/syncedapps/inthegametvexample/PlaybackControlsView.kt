package com.syncedapps.inthegametvexample

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.kaltura.android.exoplayer2.Player
import com.kaltura.android.exoplayer2.Timeline
import com.kaltura.android.exoplayer2.ui.TimeBar
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.utils.Consts
import com.kaltura.tvplayer.KalturaPlayer
import com.syncedapps.inthegametvexample.databinding.KalturaPlaybackControlViewBinding
import java.util.*

class PlaybackControlsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val formatter: Formatter
    private val formatBuilder: StringBuilder
    private val componentListener: ComponentListener
    private val updateProgressAction = Runnable { this.updateProgress() }

    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null
    private var dragging = false
    private var isPlaying : Boolean = false

    private var binding : KalturaPlaybackControlViewBinding

    init {
        binding = KalturaPlaybackControlViewBinding.inflate(LayoutInflater.from(context), this, true)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        componentListener = ComponentListener()
        initPlaybackControls()
    }

    private fun initPlaybackControls() {

        binding.kexoPlay.visibility = VISIBLE
        binding.kexoPause.visibility = INVISIBLE
        binding.kexoFfwd.visibility = View.GONE
        binding.kexoRew.visibility = View.GONE

        binding.kexoPlay.setOnClickListener(this)
        binding.kexoPause.setOnClickListener(this)
        binding.kexoFfwd.setOnClickListener(this)
        binding.kexoRew.setOnClickListener(this)

        binding.kexoProgress.addListener(componentListener)
    }


    private fun updateProgress() {
        var duration: Long? = Consts.TIME_UNSET
        var position: Long? = Consts.POSITION_UNSET.toLong()
        var bufferedPosition: Long? = 0
        if (player != null) {
            val adController = player?.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
                position = adController.adCurrentPosition

                //log.d("adController Duration:" + duration);
                //log.d("adController Position:" + position);
            } else {
                duration = player?.duration
                position = player?.currentPosition
                //log.d("Duration:" + duration);
                //log.d("Position:" + position);
                bufferedPosition = player?.bufferedPosition
            }
        }

        if (duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Duration:" + duration);
            binding.kexoDuration.text = stringForTime(duration!!)
        }

        if (!dragging && position != Consts.POSITION_UNSET.toLong() && duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Position:" + position);
            binding.kexoPosition.text = stringForTime(position!!)
            binding.kexoProgress.setPosition(position)
            binding.kexoProgress.setDuration(duration)
        }

        binding.kexoProgress.setBufferedPosition(bufferedPosition!!)
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        if (playerState != PlayerState.IDLE) {
            val delayMs: Long = 1000
            postDelayed(updateProgressAction, delayMs)
        }

        binding.kexoPlay.visibility = if (isPlaying) INVISIBLE else VISIBLE
        binding.kexoPause.visibility = if (isPlaying) VISIBLE else INVISIBLE
    }

    fun switchVisibility() {
        if (isVisible)
            hide()
        else
            show()
    }

    private fun hide() {
        this.visibility = GONE
    }

    private fun show() {
        this.visibility = VISIBLE
    }

    /**
     * Component Listener for Default time bar from ExoPlayer UI
     */
    private inner class ComponentListener : Player.Listener, TimeBar.OnScrubListener, OnClickListener {

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            dragging = true
        }

        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            binding.kexoPosition.text = stringForTime(position)
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            dragging = false
            player?.seekTo(position)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateProgress()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateProgress()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updateProgress()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            this@PlaybackControlsView.isPlaying = isPlaying
            updateProgress()
        }

        override fun onClick(view: View) {}
    }

    private fun stringForTime(timeMs: Long): String {

        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0)
            formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        else
            formatter.format("%02d:%02d", minutes, seconds).toString()
    }

    fun setPlayer(player: KalturaPlayer?) {
        this.player = player
    }

    fun setPlayerState(playerState: PlayerState) {
        this.playerState = playerState
        updateProgress()
    }

    fun setIsPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        updateProgress()
    }

    fun setSeekBarStateForAd(isAdPlaying: Boolean) {
        binding.kexoProgress.isEnabled = !isAdPlaying
    }

    override fun onClick(v: View) {
        when (v) {
            binding.kexoPlay -> {
                player?.play()
            }

            binding.kexoPause -> {
                player?.pause()
            }
        }
    }
}
