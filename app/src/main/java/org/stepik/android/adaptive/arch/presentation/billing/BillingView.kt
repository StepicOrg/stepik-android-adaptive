package org.stepik.android.adaptive.arch.presentation.billing

import org.solovyev.android.checkout.UiCheckout

interface BillingView {
    fun createUiCheckout(): UiCheckout
}
