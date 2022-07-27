package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.interaction.ITGPollAnswerView
import com.syncedapps.inthegametv.interaction.ITGPollView
import com.syncedapps.inthegametvexample.R

class CustomPollView(context: Context?) : ITGPollView(context) {

    init {
        loadLayout(R.layout.view_poll_custom)
    }

    override fun createAnswerView(): ITGPollAnswerView {
        return CustomPollAnswerView(context)
    }

    // The following override methods are all optional
    // and provide you with extra options for customization

    override fun didAnswerPoll() {
        //customize view after answering if needed
    }

    override fun didShowError(repeatedAnswer: Boolean) {
        //customize view after error if needed
    }

    override fun didLoadView() {
        //customize view at startup after it finishes loading
    }

    override fun initialFocusView(didAnswer: Boolean): View {
        //select the preferred view to get focus when the view appears
        //(this is an example implementation, if you remove the override you'll use the SDK default)
        return super.initialFocusView(didAnswer)
    }
}