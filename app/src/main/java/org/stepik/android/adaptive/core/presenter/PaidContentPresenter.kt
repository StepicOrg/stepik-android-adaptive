package org.stepik.android.adaptive.core.presenter

import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView

class PaidContentPresenter : PresenterBase<PaidContentView>() {
    companion object : PresenterFactory<PaidContentPresenter> {
        override fun create() = PaidContentPresenter()
    }

    override fun destroy() {}
}