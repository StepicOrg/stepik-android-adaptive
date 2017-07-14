package org.stepik.android.adaptive.pdd.core.presenter

import org.stepik.android.adaptive.pdd.core.presenter.contracts.StatsView

class StatsPresenter : PresenterBase<StatsView>() {
    companion object : PresenterFactory<StatsPresenter> {
        override fun create() = StatsPresenter()
    }

    override fun destroy() {}
}