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

    // The following override methods are all optional
    // and provide you with extra options for customization

    override fun didLoadView() {
        //customize view at startup after it finishes loading
    }

    override fun didAnswerTrivia() {
        //customize view after answering if needed
    }

    override fun didShowError(repeatedAnswer: Boolean) {
        //customize view after error if needed
    }

    override fun initialFocusView(didAnswer: Boolean): View {
        //select the preferred view to get focus when the view appears
        return super.initialFocusView(didAnswer)
    }
}