package org.stepik.android.adaptive.core.presenter

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.api.API
import org.stepik.android.adaptive.core.presenter.contracts.QuestionsPacksView
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.QuestionsPack
import org.stepik.android.adaptive.ui.adapter.QuestionsPacksAdapter
import org.stepik.android.adaptive.util.startPurchaseFlowRx

class QuestionsPacksPresenter : PaidContentPresenterBase<QuestionsPacksView>() {
    companion object : PresenterFactory<QuestionsPacksPresenter> {
        override fun create() = QuestionsPacksPresenter()
    }

    private val adapter = QuestionsPacksAdapter(this::onPackPressed)
    private val skus = QuestionsPack.values().map { it.id }
    private var isPacksLoaded = false

    private val compositeDisposable = CompositeDisposable()

    init {
        adapter.selection = SharedPreferenceMgr.getInstance().questionsPackIndex
    }

    fun loadContent() {
        view?.showContentProgress()
        compositeDisposable.add(getInventoryRx(ProductTypes.IN_APP, skus).subscribeOn(AndroidSchedulers.mainThread()).map {
            it.map {
                sku -> sku to QuestionsPack.getById(sku.id.code)!!
            }
        }.flatMap { packs ->
            val ids = packs.map { it.second.courseId }.toLongArray()
            API.getInstance().getCourses(ids).map { it.courses }.map { courses ->
                courses.mapNotNull { course ->
                    val pack = packs?.find { it.second.courseId == course.id }
                    pack?.second?.size = course.totalUnits
                    pack
                }
            }.subscribeOn(Schedulers.io())
        }.observeOn(AndroidSchedulers.mainThread()).subscribe({
            adapter.items = it
            view?.hideContentProgress()
            isPacksLoaded = true
            restorePurchases()
        }, {
            if (it is PurchasesNotSupportedException) {
                view?.onPurchasesNotSupported()
            } else {
                view?.onContentError()
            }
        }))
    }

    fun restorePurchases() {
        view?.showProgress()
        compositeDisposable.add(consume(getAllPurchases()))
    }


    private fun consume(observable: Observable<Purchase>) = observable.map {
            it.sku
        }.filter {
            skus.contains(it)
        }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).toList().subscribe({
            adapter.addOwnedContent(it)
            view?.hideProgress()
        }, {
            if (it !is BillingException || it.response != ResponseCodes.USER_CANCELED && it.response != ResponseCodes.BILLING_UNAVAILABLE) {
                view?.onPurchaseError()
            }
            view?.hideProgress()
        })


    private fun onPackPressed(sku: Sku, pack: QuestionsPack, isOwned: Boolean) {
        if (isOwned || pack.isFree) {
            changeCourse(pack)
        } else {
            purchase(sku)
        }
    }

    private fun purchase(sku: Sku) {
        val purchaseObservable = checkout?.startPurchaseFlowRx(sku) ?: Observable.empty<Purchase>()
        compositeDisposable.add(consume(purchaseObservable))
    }

    private fun changeCourse(pack: QuestionsPack) {
        view?.showProgress()
        compositeDisposable.add(API.getInstance()
                .joinCourse(pack.courseId)
                .doOnComplete {
                    SharedPreferenceMgr.getInstance().changeQuestionsPackIndex(pack.ordinal)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    adapter.selection = pack.ordinal
                    view?.hideProgress()
                }, {
                    it.printStackTrace()
                    view?.hideProgress()
                }))
    }

    override fun attachView(view: QuestionsPacksView) {
        super.attachView(view)

        view.onAdapter(adapter)

        if (isPacksLoaded) {
            view.hideContentProgress()
        } else {
            loadContent()
        }
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}