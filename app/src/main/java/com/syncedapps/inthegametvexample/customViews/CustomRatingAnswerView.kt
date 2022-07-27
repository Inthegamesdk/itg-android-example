package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGRatingAnswerView
import com.syncedapps.inthegametvexample.R

class CustomRatingAnswerView(context: Context?) : ITGRatingAnswerView(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating_answer_custom, this, true)
    }

    override fun setButtonStyleCompleted(selected: Boolean) {
    }
}