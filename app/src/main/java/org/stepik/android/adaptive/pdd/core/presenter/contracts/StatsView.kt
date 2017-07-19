package org.stepik.android.adaptive.pdd.core.presenter.contracts

import org.stepik.android.adaptive.pdd.ui.adapter.WeeksAdapter

interface StatsView {
    fun onWeeksAdapter(adapter: WeeksAdapter)
}