package com.syncedapps.inthegametvexample

import android.app.Activity
import android.os.Bundle
import com.syncedapps.inthegametv.ITGSettings

/**
 * Loads [MainFragment].
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //clearing user on each app launch
        //for testing only!
        val settings = ITGSettings(this)
        settings.clearUserToken()
    }
}
