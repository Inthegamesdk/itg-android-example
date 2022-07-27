package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGTriviaAnswerView
import com.syncedapps.inthegametvexample.R

class CustomTriviaAnswerView(context: Context?) : ITGTriviaAnswerView(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_trivia_answer_custom, this, true)
    }
}