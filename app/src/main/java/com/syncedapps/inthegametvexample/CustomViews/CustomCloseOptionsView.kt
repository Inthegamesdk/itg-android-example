package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGCloseOptionsView
import com.syncedapps.inthegametv.interaction.ITGPollAnswerView
import com.syncedapps.inthegametvexample.R

class CustomCloseOptionsView: ITGCloseOptionsView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_custom, this, true)
    }

    override fun createAnswerView(): ITGPollAnswerView {
        return CustomPollAnswerView(context)
    }

    override fun didLoadView() {
    }
}