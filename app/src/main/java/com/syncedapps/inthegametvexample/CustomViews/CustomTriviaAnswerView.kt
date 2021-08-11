package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGTriviaAnswerView
import com.syncedapps.inthegametvexample.R
import kotlinx.android.synthetic.main.view_poll_answer_custom.view.*

class CustomTriviaAnswerView: ITGTriviaAnswerView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_trivia_answer_custom, this, true)
    }

    override fun setButtonStyleCorrect() {
    }

    override fun setButtonStyleIncorrect() {
    }
}