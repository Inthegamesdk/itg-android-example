package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.ITGNotice
import com.syncedapps.inthegametvexample.R

class CustomNoticeView: ITGNotice {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_notice_custom, this, true)
    }
}