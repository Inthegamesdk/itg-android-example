package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.ITGRatingAnswerView
import com.syncedapps.inthegametv.ITGRatingView
import com.syncedapps.inthegametvexample.R

class CustomRatingView: ITGRatingView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_rating_custom, this, true)
    }

    override fun createAnswerView(): ITGRatingAnswerView {
        return CustomRatingAnswerView(context)
    }
}