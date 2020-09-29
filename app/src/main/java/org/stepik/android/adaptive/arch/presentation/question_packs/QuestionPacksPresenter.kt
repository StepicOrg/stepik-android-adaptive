package org.stepik.android.adaptive.arch.presentation.question_packs

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.arch.domain.question_packs.interactor.QuestionPacksBillingInteractor
import org.stepik.android.adaptive.arch.domain.question_packs.interactor.QuestionPacksInteractor
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.content.questions.QuestionsPacksResolver
import org.stepik.android.adaptive.core.presenter.PresenterBase
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
    private val questionsPacksManager: QuestionsPacksManager,
    private val questionsPacksResolver: QuestionsPacksResolver,
    private val questionPacksInteractor: QuestionPacksInteractor,
    private val questionPacksBillingInteractor: QuestionPacksBillingInteractor
) : PresenterBase<QuestionPacksView>() {
    private var state: QuestionPacksView.State = QuestionPacksView.State.Idle
        set (value) {
            field = value
            view?.setState(value)
        }

    private val compositeDisposable = CompositeDisposable()

    private var uiCheckout: UiCheckout? = null

    override fun attachView(view: QuestionPacksView) {
        super.attachView(view)

        uiCheckout = view
            .createUiCheckout()
            .also(UiCheckout::start)
    }

    fun loadQuestionListItems(forceUpdate: Boolean = false) {
        if (state == QuestionPacksView.State.Idle || (forceUpdate && state is QuestionPacksView.State.Error)) {
            state = QuestionPacksView.State.Loading
            val ids = questionsPacksManager.questionsPacks.map { it.courseId }
            compositeDisposable += questionPacksInteractor
                .getQuestionListItems(*ids.toLongArray())
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        state = QuestionPacksView.State.QuestionPacksLoaded(it)
                    },
                    onError = {
                        state = QuestionPacksView.State.Error
                    }
                )
        }
    }

    fun purchaseCourse(courseId: Long, sku: Sku) {
        val checkout = this.uiCheckout
            ?: return

        compositeDisposable += questionPacksBillingInteractor
            .purchaseCourse(checkout, courseId, sku)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {

                }
            )
    }

    fun restoreCoursePurchase(sku: Sku) {
        compositeDisposable += questionPacksBillingInteractor
            .restorePurchase(sku)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onError = {

                }
            )
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