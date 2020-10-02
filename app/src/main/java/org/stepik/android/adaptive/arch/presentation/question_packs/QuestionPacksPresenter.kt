package org.stepik.android.adaptive.arch.presentation.question_packs

import android.util.Log
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.arch.domain.question_packs.interactor.QuestionPacksBillingInteractor
import org.stepik.android.adaptive.arch.domain.question_packs.interactor.QuestionPacksInteractor
import org.stepik.android.adaptive.arch.domain.question_packs.model.EnrollmentState
import org.stepik.android.adaptive.arch.presentation.question_packs.mapper.toEnrollmentError
import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class QuestionPacksPresenter
@Inject
constructor(
    private val api: Api,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val questionsPacksManager: QuestionsPacksManager,
    private val questionPacksInteractor: QuestionPacksInteractor,
    private val questionPacksBillingInteractor: QuestionPacksBillingInteractor
) : PresenterBase<QuestionPacksView>() {
    private var state: QuestionPacksView.State = QuestionPacksView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private var uiCheckout: UiCheckout? = null

    override fun attachView(view: QuestionPacksView) {
        super.attachView(view)

        uiCheckout = view
            .createUiCheckout()
            .also(UiCheckout::start)
    }

    fun loadQuestionListItems(forceUpdate: Boolean = false) {
        if (state == QuestionPacksView.State.Idle || forceUpdate) {
            state = QuestionPacksView.State.Loading
            val ids = questionsPacksManager.questionsPacks.map { it.courseId }
            compositeDisposable += questionPacksInteractor
                .getQuestionListItems(ids)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { questionListItems ->
                        state = QuestionPacksView.State.QuestionPacksLoaded(questionListItems)
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

        view?.showProgress()

        compositeDisposable += questionPacksBillingInteractor
            .purchaseCourse(checkout, courseId, sku)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .doFinally {
                view?.hideProgress()
            }
            .subscribeBy(
                onComplete = {
                    val oldState = (state as? QuestionPacksView.State.QuestionPacksLoaded)
                        ?: return@subscribeBy

                    val updatedQuestionItemList = oldState.questionItemList.map {
                        if (it.course.id == courseId) {
                            it.copy(enrollmentState = EnrollmentState.Enrolled)
                        } else {
                            it
                        }
                    }

                    state = QuestionPacksView.State.QuestionPacksLoaded(questionItemList = updatedQuestionItemList)
                    updatedQuestionItemList.find { it.id == courseId }?.let {
                        view?.changeAfterPurchase(it)
                        changeCourse(it.questionPack)
                    }
                },
                onError = {
                    view?.showEnrollmentError(it.toEnrollmentError())
                }
            )
    }

    fun restoreCoursePurchases() {
        val skus = (state as? QuestionPacksView.State.QuestionPacksLoaded)
            ?.questionItemList
            ?.mapNotNull { (it.enrollmentState as? EnrollmentState.NotEnrolledInApp)?.skuWrapper?.sku }
            ?.takeIf { it.isNotEmpty() }
            ?: return

        view?.showProgress()
        compositeDisposable += questionPacksBillingInteractor
            .restorePurchases(skus)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .doFinally {
                view?.hideProgress()
                loadQuestionListItems(forceUpdate = true)
            }
            .subscribeBy(
                onError = {
                    view?.showEnrollmentError(it.toEnrollmentError())
                }
            )
    }

    fun changeCourse(pack: QuestionsPack) {
        view?.showProgress()
        compositeDisposable += api
            .joinCourse(pack.courseId)
            .doOnComplete {
                questionsPacksManager.switchPack(pack)
            }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribe(
                {
                    view?.hideProgress()
                },
                {
                    view?.hideProgress()
                }
            )
    }

    override fun detachView(view: QuestionPacksView) {
        super.detachView(view)

        uiCheckout?.let(UiCheckout::stop)
        uiCheckout = null
    }
}
