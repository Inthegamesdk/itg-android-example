package com.syncedapps.inthegametvexample

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.ITGPollAnswerView
import com.syncedapps.inthegametv.ITGPollView
import com.syncedapps.inthegametvexample.CustomViews.CustomPollAnswerView
import kotlinx.android.synthetic.main.view_poll_custom.view.*

class CustomPollView: ITGPollView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_custom, this, true)
    }

    override fun createAnswerView(): ITGPollAnswerView {
        return CustomPollAnswerView(context)
    }

    override fun didAnswerPoll() {
        //customize view after answering if needed
    }
}