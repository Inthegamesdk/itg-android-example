package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.interaction.ITGCloseOptionsView
import com.syncedapps.inthegametv.interaction.ITGPollAnswerView
import com.syncedapps.inthegametvexample.R

class CustomCloseOptionsView(context: Context?) : ITGCloseOptionsView(context) {

    init {
        loadLayout(R.layout.view_poll_custom)
    }

    override fun createAnswerView(): ITGPollAnswerView {
        return CustomPollAnswerView(context)
    }

    override fun didLoadView() {
    }
}