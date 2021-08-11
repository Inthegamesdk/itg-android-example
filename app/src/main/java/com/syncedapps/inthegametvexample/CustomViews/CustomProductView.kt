package com.syncedapps.inthegametvdemo.CustomViews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.syncedapps.inthegametv.interaction.ITGProductView
import com.syncedapps.inthegametvexample.R

class CustomProductView : ITGProductView {
    constructor(context: Context?) : super(context)

    init {
        val c = ContextThemeWrapper(context, R.style.Theme_AppCompat)
        LayoutInflater.from(c).inflate(R.layout.view_product_custom, this, true)
    }

    override fun didLoadView() {
    }

    override fun didShowError() {
    }
}