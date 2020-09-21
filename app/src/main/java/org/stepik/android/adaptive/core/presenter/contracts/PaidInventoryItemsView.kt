package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter

interface PaidInventoryItemsView : PaidContentView {
    fun showInventoryDialog()
    fun onAdapter(adapter: PaidInventoryAdapter)
}
