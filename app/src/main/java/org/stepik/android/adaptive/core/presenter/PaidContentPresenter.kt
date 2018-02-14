package org.stepik.android.adaptive.core.presenter

import android.content.Intent
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView
import org.stepik.android.adaptive.ui.adapter.PaidContentAdapter
import org.stepik.android.adaptive.util.InventoryUtil

class PaidContentPresenter : PresenterBase<PaidContentView>() {
    companion object : PresenterFactory<PaidContentPresenter> {
        override fun create() = PaidContentPresenter()
    }

    private val adapter = PaidContentAdapter(this::purchase)
    private var checkout: ActivityCheckout? = null

    private var isInventoryLoaded = false

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
        view?.getBilling()?.newRequestsBuilder()?.create()?.getPurchases(ProductTypes.IN_APP, continuationToken, object : RequestListener<Purchases> {
            override fun onSuccess(purchases: Purchases) {
                purchases.list.forEach {
                    consume(it, InventoryUtil.PaidContent.getById(it.sku)!!)
                }
                purchases.continuationToken?.let {
                    restorePurchases(it)
                }
            }

            override fun onError(response: Int, exception: Exception) {
                view?.onPurchaseError()
            }
        })
    }

    private fun consume(purchase: Purchase, paidContent: InventoryUtil.PaidContent, withAnimation: Boolean = false) {
        checkout?.whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                requests.consume(purchase.token, object : RequestListener<Any> {
                    override fun onSuccess(result: Any) {
                        InventoryUtil.changeItemCount(paidContent.item, paidContent.count.toLong())
                        if (withAnimation) {
                            view?.showInventoryDialog()
                        }
                    }

                    override fun onError(response: Int, exception: Exception) {
                        view?.onPurchaseError()
                    }
                })
            }
        })
    }

    fun loadInventory() {
        view?.onInventoryLoading()
        val request = Inventory.Request.create()
        request.loadAllPurchases()
        request.loadSkus(ProductTypes.IN_APP, InventoryUtil.PaidContent.values().map { it.id })
        checkout?.loadInventory(request) {
            val product = it.get(ProductTypes.IN_APP)
            if (product.supported) {
                adapter.items = product.skus.map { sku ->
                    sku to InventoryUtil.PaidContent.getById(sku.id.code)!!
                }
            }
            view?.onInventoryLoaded()
            isInventoryLoaded = true
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        checkout?.onActivityResult(requestCode, resultCode, data)

    override fun attachView(view: PaidContentView) {
        super.attachView(view)
        checkout = view.createCheckout()
        checkout?.start()

        view.onAdapter(adapter)

        if (isInventoryLoaded) {
            view.onInventoryLoaded()
        } else {
            loadInventory()
        }
    }

    override fun detachView(view: PaidContentView) {
        checkout?.stop()
        checkout = null
        super.detachView(view)
    }

    override fun destroy() {
        checkout?.stop()
        checkout = null
    }
}