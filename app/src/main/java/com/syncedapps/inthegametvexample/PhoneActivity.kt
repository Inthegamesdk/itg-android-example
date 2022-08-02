package com.syncedapps.inthegametvexample

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.syncedapps.inthegametv.ITGSettings
import com.syncedapps.inthegametvexample.databinding.ActivityPhoneBinding

class PhoneActivity : Activity() {

    private lateinit var binding: ActivityPhoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhoneBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val settings = ITGSettings(this)
        settings.clearUserToken()

        binding.startButton.setOnClickListener {
            val intent = Intent(this, PlaybackPhoneActivity::class.java)
            startActivity(intent)
        }
    }
}