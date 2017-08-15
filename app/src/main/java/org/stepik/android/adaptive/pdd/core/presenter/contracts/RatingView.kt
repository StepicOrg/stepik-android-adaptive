package org.stepik.android.adaptive.pdd.core.presenter.contracts

import org.stepik.android.adaptive.pdd.ui.adapter.RatingAdapter

interface RatingView {
    fun onLoading()
    fun onError()
    fun onComplete()
    fun onRatingAdapter(adapter: RatingAdapter)
}