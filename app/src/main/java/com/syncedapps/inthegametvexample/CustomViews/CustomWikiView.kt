package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.interaction.ITGWikiView
import com.syncedapps.inthegametvexample.R
import kotlinx.android.synthetic.main.view_poll_custom.view.*

class CustomWikiView: ITGWikiView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_wiki_custom, this, true)
    }

    // The following override methods are all optional
    // and provide you with extra options for customization

    override fun didLoadView() {
        //customize view at startup after it finishes loading
    }

    override fun initialFocusView(): View {
        //select the preferred view to get focus when the view appears
        return super.initialFocusView()
    }
}