package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.ITGIconPosition
import com.syncedapps.inthegametv.ITGPollAnswerView
import com.syncedapps.inthegametvexample.R
import kotlinx.android.synthetic.main.view_poll_answer_custom.view.answerButton

class CustomPollAnswerView: ITGPollAnswerView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_answer_custom, this, true)
        iconPosition = ITGIconPosition.RIGHT
    }

    override fun setButtonStyleSelected() {
        answerButton.setBackgroundResource(R.drawable.button_answer_selector_correct)
    }
}