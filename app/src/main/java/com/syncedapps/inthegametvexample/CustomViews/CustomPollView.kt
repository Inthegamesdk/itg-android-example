package com.syncedapps.inthegametvexample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.ITGPollAnswerView
import com.syncedapps.inthegametv.ITGPollView
import com.syncedapps.inthegametv.PollAnswer
import com.syncedapps.inthegametvexample.CustomViews.CustomPollAnswerView
import kotlinx.android.synthetic.main.view_poll_answer_custom.view.*
import kotlinx.android.synthetic.main.view_poll_custom.view.*

class CustomPollView: ITGPollView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_poll_custom, this, true)
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
        if (didAnswer) {
            return super.initialFocusView(didAnswer)
        } else {
            return getAnswerViews().randomOrNull() ?: smallCloseButton
        }
    }
}