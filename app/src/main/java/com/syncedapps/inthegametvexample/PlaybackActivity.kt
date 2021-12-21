package com.syncedapps.inthegametvexample

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity

/** Loads [PlaybackVideoFragment]. */
class PlaybackActivity : FragmentActivity() {
    val playbackFragmentTag = "playbackFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, PlaybackVideoFragment(), playbackFragmentTag)
                    .commit()
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
        if (fragment != null && fragment.handleBackPressIfNeeded()) {
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            val fragment =
                supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
            fragment?.receivedKeyEvent(event)
        }
        return super.onKeyDown(keyCode, event)
    }
}
