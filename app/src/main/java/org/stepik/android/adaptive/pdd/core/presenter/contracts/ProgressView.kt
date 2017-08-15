package org.stepik.android.adaptive.pdd.core.presenter.contracts

import org.stepik.android.adaptive.pdd.ui.adapter.WeeksAdapter

interface ProgressView {
    fun onWeeksAdapter(adapter: WeeksAdapter)
}