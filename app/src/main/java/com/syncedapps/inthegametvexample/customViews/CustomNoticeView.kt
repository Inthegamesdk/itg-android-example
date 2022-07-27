package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGNotice
import com.syncedapps.inthegametvexample.R

class CustomNoticeView(context: Context) : ITGNotice(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_notice_custom, this, true)
    }
}