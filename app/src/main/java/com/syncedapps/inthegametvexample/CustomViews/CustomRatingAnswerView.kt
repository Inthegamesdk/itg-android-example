package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGRatingAnswerView
import com.syncedapps.inthegametvexample.R
import kotlinx.android.synthetic.main.view_rating_answer_custom.view.answerButton

class CustomRatingAnswerView: ITGRatingAnswerView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating_answer_custom, this, true)
    }

    override fun setButtonStyleCompleted(selected: Boolean) {
    }
}