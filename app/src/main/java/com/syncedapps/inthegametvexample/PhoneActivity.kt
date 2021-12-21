package com.syncedapps.inthegametvexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.syncedapps.inthegametv.ITGSettings
import kotlinx.android.synthetic.main.activity_phone.*

class PhoneActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val settings = ITGSettings(this)
        settings.clearUserToken()

        startButton.setOnClickListener {
            val intent = Intent(this, PlaybackPhoneActivity::class.java)
            startActivity(intent)
        }
    }
}