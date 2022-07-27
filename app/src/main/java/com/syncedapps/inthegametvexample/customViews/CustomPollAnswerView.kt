package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGPollAnswerView
import com.syncedapps.inthegametvexample.R

class CustomPollAnswerView(context: Context?) : ITGPollAnswerView(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_answer_custom, this, true)
    }

}