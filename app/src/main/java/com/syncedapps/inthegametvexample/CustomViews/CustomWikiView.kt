package com.syncedapps.inthegametvexample.CustomViews

import android.content.Context
import android.view.LayoutInflater
import com.syncedapps.inthegametv.ITGRatingView
import com.syncedapps.inthegametv.ITGWikiView
import com.syncedapps.inthegametvexample.R

class CustomWikiView: ITGWikiView {
    constructor(context: Context?) : super(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_wiki_custom, this, true)
    }
}