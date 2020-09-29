package org.stepik.android.adaptive.arch.presentation.question_packs

import org.solovyev.android.checkout.UiCheckout

interface QuestionPacksView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        object PurchasesNotSupported : State()

        class QuestionPacksLoaded() : State()
    }

    fun setState(state: State)

    fun showProgress()
    fun hideProgress()

    fun createUiCheckout(): UiCheckout
}