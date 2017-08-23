package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.WeeksAdapter

interface ProgressView {
    fun onWeeksAdapter(adapter: WeeksAdapter)
}