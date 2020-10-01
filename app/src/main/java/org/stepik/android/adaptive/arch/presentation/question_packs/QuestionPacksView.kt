package org.stepik.android.adaptive.arch.presentation.question_packs

import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.arch.domain.question_packs.model.QuestionListItem
import org.stepik.android.adaptive.arch.presentation.question_packs.model.EnrollmentError

interface QuestionPacksView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        object PurchasesNotSupported : State()

        data class QuestionPacksLoaded(val questionItemList: List<QuestionListItem>) : State()
    }

    fun setState(state: State)

    fun showProgress()
    fun hideProgress()

    fun createUiCheckout(): UiCheckout
    fun showEnrollmentError(errorType: EnrollmentError)
}
