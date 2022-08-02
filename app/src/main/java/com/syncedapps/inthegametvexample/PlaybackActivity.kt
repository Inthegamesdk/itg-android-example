package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity

/** Loads [PlaybackVideoFragment]. */
class PlaybackActivity : FragmentActivity() {
    private val playbackFragmentTag = "playbackFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PlaybackVideoFragment(), playbackFragmentTag)
                .commit()
        }
    }

    override fun onBackPressed() {
        val fragment =
            supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
        if (fragment != null && fragment.handleBackPressIfNeeded()) {
            return
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val fragment =
            supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
        return (event.action == KeyEvent.ACTION_DOWN && fragment?.receivedKeyEvent(event) ?: false) || super.dispatchKeyEvent(
            event
        )
    }
}
