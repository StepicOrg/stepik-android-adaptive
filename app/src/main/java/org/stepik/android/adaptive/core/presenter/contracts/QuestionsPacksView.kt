package org.stepik.android.adaptive.core.presenter.contracts

import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView
import org.stepik.android.adaptive.ui.adapter.QuestionsPacksAdapter

interface QuestionsPacksView : PaidContentView {
    fun onAdapter(adapter: QuestionsPacksAdapter)
    fun onContentError()
    fun createUiCheckout(): UiCheckout
}
