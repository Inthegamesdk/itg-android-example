package com.syncedapps.inthegametvexample

import android.view.KeyEvent
import androidx.leanback.app.VideoSupportFragment

open class VideoSupportFragment : VideoSupportFragment() {
    open fun dispatchKeyEvent(event: KeyEvent?): Boolean = false

    open fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = false

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false
}