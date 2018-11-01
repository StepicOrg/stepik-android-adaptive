package org.stepik.android.adaptive.core.presenter

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.core.presenter.contracts.PaidInventoryItemsView
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.InventoryManager
import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter
import org.stepik.android.adaptive.util.*
import javax.inject.Inject

class PaidInventoryItemsPresenter
@Inject
constructor(
        private val analytics: Analytics,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val inventoryManager: InventoryManager,
        billing: Billing
): PaidContentPresenterBase<PaidInventoryItemsView>(billing) {
    private val skus = InventoryManager.PaidContent.ids.toList()
    private val adapter = PaidInventoryAdapter(this::purchase)
    private val compositeDisposable = CompositeDisposable()
    private var isInventoryLoaded = false

    private fun onRestoreTaskCompleted() {
        skipUIFrame({ view?.hideProgress() })
        view?.showInventoryDialog()
    }

    private fun purchase(sku: Sku, paidContent: InventoryManager.PaidContent) {
        val purchaseObservable = checkout?.startPurchaseFlowRx(sku)?.doOnNext {
            analytics.logAmplitudePurchase(AmplitudeAnalytics.Tickets.TICKETS_PURCHASED, sku, mapOf(
                    AmplitudeAnalytics.Tickets.PARAM_TICKETS_COUNT to paidContent.count
            ))
        } ?: Observable.empty<Purchase>()
        compositeDisposable addDisposable consume(purchaseObservable)
    }

    fun restorePurchases() {
        view?.showProgress()
        compositeDisposable addDisposable consume(getAllPurchases())
    }

    private fun consume(observable: Observable<Purchase> /* , some additional info */): Disposable = observable.mapNotNull {
        InventoryManager.PaidContent.getById(it.sku)?.to(it.token)
    }.flatMap { p ->
        checkout?.onReady()?.flatMap { it.consumeRx(p.second).andThen(Observable.just(p.first)) }
    }.subscribeOn(mainScheduler).observeOn(mainScheduler).subscribe({
        // onNext
        inventoryManager.changeItemCount(it.item, it.count.toLong())
    }, {
        // onError
        if (it !is BillingException || it.response != ResponseCodes.USER_CANCELED) {
            view?.onPurchaseError()
        }
    }, {
        // onComplete
        onRestoreTaskCompleted()
    })

    private fun loadInventory() {
        view?.showContentProgress()
        getInventory(ProductTypes.IN_APP, skus) {
            val product = it.get(ProductTypes.IN_APP)
            if (product.supported) {
                adapter.items = product.skus.map { sku ->
                    sku to InventoryManager.PaidContent.getById(sku.id.code)!!
                }
                view?.hideContentProgress()
                isInventoryLoaded = true
            } else {
                view?.onPurchasesNotSupported()
            }
        }
    }

    override fun attachView(view: PaidInventoryItemsView) {
        super.attachView(view)
        view.onAdapter(adapter)

        if (isInventoryLoaded) {
            view.hideContentProgress()
        } else {
            loadInventory()
        }
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}