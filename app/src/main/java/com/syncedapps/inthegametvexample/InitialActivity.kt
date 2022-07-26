package com.syncedapps.inthegametvexample

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

class InitialActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isTV(this))
            startActivity(Intent(this, MainActivity::class.java))
        else
            startActivity(Intent(this, PhoneActivity::class.java))
        finishAffinity()
    }

    private fun isTV(context: Context?): Boolean {
        val uiModeManager =
            context?.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager ?: return false
        return (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }
}