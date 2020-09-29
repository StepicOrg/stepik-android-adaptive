package org.stepik.android.adaptive.arch.presentation.question_packs

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.arch.domain.billing.repository.BillingRepository
import org.stepik.android.adaptive.arch.domain.question_packs.QuestionPacksBillingInteractor
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.content.questions.QuestionsPacksResolver
import org.stepik.android.adaptive.core.presenter.PresenterBase
import org.stepik.android.adaptive.core.presenter.contracts.QuestionsPacksView
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import javax.inject.Inject

class QuestionPacksPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val billingRepository: BillingRepository,
    private val questionsPacksManager: QuestionsPacksManager,
    private val questionsPacksResolver: QuestionsPacksResolver,
    private val questionPacksBillingInteractor: QuestionPacksBillingInteractor
) : PresenterBase<QuestionPacksView>() {
    private val compositeDisposable = CompositeDisposable()

    private var uiCheckout: UiCheckout? = null
    private val courseIds = questionsPacksManager.ids
    private val skus = questionsPacksManager.skus

    override fun attachView(view: QuestionPacksView) {
        super.attachView(view)

        uiCheckout = view
            .createUiCheckout()
            .also(UiCheckout::start)
    }

    override fun detachView(view: QuestionPacksView) {
        super.detachView(view)

        uiCheckout?.let(UiCheckout::stop)
        uiCheckout = null
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}