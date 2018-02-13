package org.stepik.android.adaptive.core.presenter.contracts

import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing
import org.stepik.android.adaptive.ui.adapter.PaidContentAdapter

interface PaidContentView {
    fun showInventoryDialog()
    fun onAdapter(adapter: PaidContentAdapter)

    fun createCheckout(): ActivityCheckout
    fun getBilling(): Billing

    fun onInventoryError()
    fun onInventoryLoading()
    fun onInventoryLoaded()

    fun onPurchaseError()
}