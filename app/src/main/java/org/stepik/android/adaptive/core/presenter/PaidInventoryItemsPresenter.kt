package org.stepik.android.adaptive.core.presenter

import android.content.Intent
import io.reactivex.Observable
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.core.presenter.contracts.PaidInventoryItemsView
import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter
import org.stepik.android.adaptive.util.*
import java.util.concurrent.atomic.AtomicInteger

class PaidInventoryItemsPresenter : PresenterBase<PaidInventoryItemsView>() {
    companion object : PresenterFactory<PaidInventoryItemsPresenter> {
        override fun create() = PaidInventoryItemsPresenter()
    }

    private val adapter = PaidInventoryAdapter(this::purchase)
    private var checkout: ActivityCheckout? = null

    private var isInventoryLoaded = false

    private val activeRestoreTasks = AtomicInteger(0)

    private fun onRestoreTaskStarted() {
        activeRestoreTasks.incrementAndGet()
    }

    private fun onRestoreTaskCompleted(withAnimation: Boolean = false) {
        if (activeRestoreTasks.decrementAndGet() == 0) {
            skipUIFrame({ view?.onRestored() })
            if (withAnimation) {
                view?.showInventoryDialog()
            }
        }
    }

    private fun purchase(sku: Sku, paidContent: InventoryUtil.PaidContent) {
        checkout?.startPurchaseFlow(sku, null, object : RequestListener<Purchase> {
            override fun onSuccess(purchase: Purchase) {
                consume(purchase, paidContent, true)
            }

            override fun onError(response: Int, exception: Exception) {
                if (response != ResponseCodes.USER_CANCELED) {
                    view?.onPurchaseError()
                }
            }
        })
    }

    fun restorePurchases(continuationToken: String? = null) {
        view?.getBilling()?.newRequestsBuilder()?.create()?.let {
            if (continuationToken == null) {
                view?.onRestoreLoading()
            }
            onRestoreTaskStarted()
            it.getPurchases(ProductTypes.IN_APP, continuationToken, object : RequestListener<Purchases> {
                override fun onSuccess(purchases: Purchases) {
                    purchases.list.forEach { purchase ->
                        InventoryUtil.PaidContent.getById(purchase.sku)?.let {
                            consume(purchase, it)
                        }
                    }
                    purchases.continuationToken?.let {
                        restorePurchases(it)
                    }
                    onRestoreTaskCompleted(false)
                }

                override fun onError(response: Int, exception: Exception) {
                    exception.printStackTrace()
                    view?.onPurchaseError()
                    onRestoreTaskCompleted()
                }
            })
        }
    }

    private fun getPurchases(continuationToken: String? = null) =
            view?.getBilling()?.newRequestsBuilder()?.create()?.getPurchasesRx(ProductTypes.IN_APP, continuationToken) ?: Observable.empty<Purchases>()

    private fun getAllPurchases(): Observable<Purchase> = getPurchases().concatMap {
        Observable.just(it).concatWith(getPurchases(it.continuationToken))
    }.concatMap {
        Observable.fromIterable(it.list)
    }

    private fun restore() {
        getAllPurchases().flatMap { purchase ->
            checkout?.onReady()?.flatMap { it.consumeRx(purchase.token).andThen(Observable.just(purchase)) }
        }.toList().subscribe { _, _ ->
            onRestoreTaskCompleted()
        }
    }

    private fun consume(purchase: Purchase, paidContent: InventoryUtil.PaidContent, withAnimation: Boolean = false) {
        checkout?.let {
            onRestoreTaskStarted()
            it.whenReady(object : Checkout.EmptyListener() {
                override fun onReady(requests: BillingRequests) {
                    requests.consume(purchase.token, object : RequestListener<Any> {
                        override fun onSuccess(result: Any) {
                            InventoryUtil.changeItemCount(paidContent.item, paidContent.count.toLong())
                            if (withAnimation) {
                                view?.showInventoryDialog()
                            }
                            onRestoreTaskCompleted(!withAnimation)
                        }

                        override fun onError(response: Int, exception: Exception) {
                            view?.onPurchaseError()
                            onRestoreTaskCompleted()
                        }
                    })
                }
            })
        }
    }

    private fun loadInventory() {
        view?.onContentLoading()
        val request = Inventory.Request.create()
        request.loadAllPurchases()
        request.loadSkus(ProductTypes.IN_APP, InventoryUtil.PaidContent.values().map { it.id })
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

        activeRestoreTasks.set(0)
        view.onRestored()

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
        checkout?.stop()
        checkout = null
    }
}