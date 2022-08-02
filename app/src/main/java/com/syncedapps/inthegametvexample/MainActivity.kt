package com.syncedapps.inthegametvexample

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.syncedapps.inthegametv.ITGSettings

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //clearing user on each app launch
        //for testing only!
        val settings = ITGSettings(this)
        settings.clearUserToken()
    }
}
