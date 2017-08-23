package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.RatingAdapter

interface RatingView {
    fun onLoading()
    fun onConnectivityError()
    fun onRequestError()
    fun onComplete()
    fun onRatingAdapter(adapter: RatingAdapter)
}