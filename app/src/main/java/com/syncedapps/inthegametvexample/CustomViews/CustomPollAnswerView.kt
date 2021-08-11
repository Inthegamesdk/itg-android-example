package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGIconPosition
import com.syncedapps.inthegametv.interaction.ITGPollAnswerView
import com.syncedapps.inthegametvexample.R
import kotlinx.android.synthetic.main.view_poll_answer_custom.view.*

class CustomPollAnswerView: ITGPollAnswerView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_answer_custom, this, true)
        iconPosition = ITGIconPosition.RIGHT
    }

    override fun setButtonStyleCompleted(selected: Boolean) {
        tvTitle.textSize = 20f
        if (selected) {
            tvTitle.setTextColor(resources.getColor(R.color.green_bright))
            tvAnswerStats.setTextColor(resources.getColor(R.color.green_bright))
        }
    }
}