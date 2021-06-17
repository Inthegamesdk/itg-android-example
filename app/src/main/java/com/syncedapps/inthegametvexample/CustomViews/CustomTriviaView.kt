package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.ITGTriviaAnswerView
import com.syncedapps.inthegametv.ITGTriviaView
import com.syncedapps.inthegametvexample.R

class CustomTriviaView: ITGTriviaView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_trivia_custom, this, true)
    }

    override fun createAnswerView(): ITGTriviaAnswerView {
        return CustomTriviaAnswerView(context)
    }

    override fun didAnswerTrivia() {
        //customize view after answering if needed
    }

    override fun didShowError() {
        //customize view after error if needed
    }
}