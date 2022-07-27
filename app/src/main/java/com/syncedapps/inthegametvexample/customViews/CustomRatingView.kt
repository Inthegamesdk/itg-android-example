package com.syncedapps.inthegametvexample.customViews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.syncedapps.inthegametv.interaction.ITGRatingAnswerView
import com.syncedapps.inthegametv.interaction.ITGRatingView
import com.syncedapps.inthegametvexample.R

class CustomRatingView(context: Context?) : ITGRatingView(context) {

    init {
        loadLayout(R.layout.view_rating_custom)
    }

    override fun createAnswerView(): ITGRatingAnswerView {
        return CustomRatingAnswerView(context)
    }

    // The following override methods are all optional
    // and provide you with extra options for customization

    override fun didLoadView() {
        //customize view at startup after it finishes loading
    }

    override fun didAnswerRating() {
        // customize view after answering if needed
    }

    override fun didUpdateStats(average: Double, totalVotes: Int) {
        // this will be called everytime the rating results are updated,
        // if you need to display the values in a custom way
    }

    override fun didShowError(repeatedAnswer: Boolean) {
        //customize view after error if needed
    }

    override fun initialFocusView(didAnswer: Boolean): View {
        //select the preferred view to get focus when the view appears
        return super.initialFocusView(didAnswer)
    }
}