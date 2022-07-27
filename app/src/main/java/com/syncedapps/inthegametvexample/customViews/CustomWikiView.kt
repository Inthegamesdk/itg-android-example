package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.interaction.ITGWikiView
import com.syncedapps.inthegametvexample.R

class CustomWikiView(context: Context?) : ITGWikiView(context) {

    init {
        loadLayout(R.layout.view_wiki_custom)
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