package org.stepik.android.adaptive.core.presenter.contracts

import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing

interface PaidContentView {
    fun createCheckout(): ActivityCheckout
    fun getBilling(): Billing

    fun onContentLoading()
    fun onContentLoaded()
    fun onPurchasesNotSupported()

    fun showProgress()
    fun hideProgress()
    fun onPurchaseError()
}