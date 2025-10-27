package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import android.view.ViewGroup
import java.util.*

import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var itgVideoView : ITGVideoView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()
        itgVideoView = ITGVideoView(this, Const.VIDEO_URL)
        binding.outerContainer.addView(itgVideoView)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (itgVideoView == null || itgVideoView?.onBackPressed() == false) {
                    // Implement your own back press action here
                    finish()
                }
            }
        })
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

}