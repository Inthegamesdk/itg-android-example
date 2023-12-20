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

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if((supportFragmentManager.findFragmentById(android.R.id.content) as? VideoSupportFragment)?.dispatchKeyEvent(event) != true)
            super.dispatchKeyEvent(event)
        else
            true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if((supportFragmentManager.findFragmentById(android.R.id.content) as? VideoSupportFragment)?.onKeyUp(keyCode, event) != true)
            super.onKeyUp(keyCode, event)
        else
            true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if((supportFragmentManager.findFragmentById(android.R.id.content) as? VideoSupportFragment)?.onKeyDown(keyCode, event) != true)
            super.onKeyDown(keyCode, event)
        else
            true
    }

}
