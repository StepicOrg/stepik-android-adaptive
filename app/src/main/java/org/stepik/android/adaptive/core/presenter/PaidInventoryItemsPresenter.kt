package org.stepik.android.adaptive.core.presenter

import android.content.Intent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.core.presenter.contracts.PaidInventoryItemsView
import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter
import org.stepik.android.adaptive.util.*

class PaidInventoryItemsPresenter : PresenterBase<PaidInventoryItemsView>() {
    companion object : PresenterFactory<PaidInventoryItemsPresenter> {
        override fun create() = PaidInventoryItemsPresenter()
    }

    private val adapter = PaidInventoryAdapter(this::purchase)
    private var checkout: ActivityCheckout? = null

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

    private fun createPurchasesObservable(continuationToken: String? = null) =
            view?.getBilling()?.newRequestsBuilder()?.create()?.getPurchasesRx(ProductTypes.IN_APP, continuationToken) ?: Observable.empty<Purchases>()

    private fun getAllPurchases(): Observable<Purchase> = createPurchasesObservable().concatMap {
        Observable.just(it).concatWith(createPurchasesObservable(it.continuationToken))
    }.concatMap {
        Observable.fromIterable(it.list)
    }

    fun restorePurchases() = compositeDisposable.add(consume(getAllPurchases()))

    private fun consume(observable: Observable<Purchase> /* , some additional info */): Disposable = observable.mapNotNull {
        InventoryUtil.PaidContent.getById(it.sku)?.to(it.token)
    }.flatMap { p ->
        checkout?.onReady()?.flatMap { it.consumeRx(p.second).andThen(Observable.just(p.first)) }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
        // onNext
        InventoryUtil.changeItemCount(it.item, it.count.toLong())
    }, {
        // onError
        view?.onPurchaseError()
    }, {
        // onComplete
        onRestoreTaskCompleted()
    })

    private fun loadInventory() {
        view?.onContentLoading()
        val request = Inventory.Request.create()
        request.loadAllPurchases()
        request.loadSkus(ProductTypes.IN_APP, InventoryUtil.PaidContent.ids.toList())
        checkout?.loadInventory(request) {
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

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        checkout?.onActivityResult(requestCode, resultCode, data)

    override fun attachView(view: PaidInventoryItemsView) {
        super.attachView(view)
        checkout = view.createCheckout()
        checkout?.start()

        view.onAdapter(adapter)

        if (isInventoryLoaded) {
            view.onContentLoaded()
        } else {
            loadInventory()
        }
    }

    override fun detachView(view: PaidInventoryItemsView) {
        checkout?.stop()
        checkout = null
        super.detachView(view)
    }

    override fun destroy() {
        compositeDisposable.dispose()
        checkout?.stop()
        checkout = null
    }
}