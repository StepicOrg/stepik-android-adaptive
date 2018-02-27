package org.stepik.android.adaptive.core.presenter

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.core.presenter.contracts.PaidInventoryItemsView
import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter
import org.stepik.android.adaptive.util.*

class PaidInventoryItemsPresenter : PaidContentPresenterBase<PaidInventoryItemsView>() {
    companion object : PresenterFactory<PaidInventoryItemsPresenter> {
        override fun create() = PaidInventoryItemsPresenter()
    }

    private val skus = InventoryUtil.PaidContent.ids.toList()
    private val adapter = PaidInventoryAdapter(this::purchase)
    private val compositeDisposable = CompositeDisposable()
    private var isInventoryLoaded = false

    private fun onRestoreTaskCompleted() {
        skipUIFrame({ view?.onRestored() })
        view?.showInventoryDialog()
    }

    private fun purchase(sku: Sku) {
        val purchaseObservable = checkout?.startPurchaseFlowRx(sku) ?: Observable.empty<Purchase>()
        compositeDisposable.add(consume(purchaseObservable))
    }

    fun restorePurchases() = compositeDisposable.add(consume(getAllPurchases()))

    private fun consume(observable: Observable<Purchase> /* , some additional info */): Disposable = observable.mapNotNull {
        InventoryUtil.PaidContent.getById(it.sku)?.to(it.token)
    }.flatMap { p ->
        checkout?.onReady()?.flatMap { it.consumeRx(p.second).andThen(Observable.just(p.first)) }
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe({
        // onNext
        InventoryUtil.changeItemCount(it.item, it.count.toLong())
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
        view?.onContentLoading()
        getInventory(ProductTypes.IN_APP, skus) {
            val product = it.get(ProductTypes.IN_APP)
            if (product.supported) {
                adapter.items = product.skus.map { sku ->
                    sku to InventoryUtil.PaidContent.getById(sku.id.code)!!
                }
                view?.onContentLoaded()
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
            view.onContentLoaded()
        } else {
            loadInventory()
        }
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}