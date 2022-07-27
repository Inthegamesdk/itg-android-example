package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import com.syncedapps.inthegametv.interaction.ITGProductView
import com.syncedapps.inthegametvexample.R

class CustomProductView(context: Context?) : ITGProductView(context) {

    init {
        loadLayout(R.layout.view_product_custom)
    }

    override fun didLoadView() {
    }

    override fun didShowError() {
    }
}