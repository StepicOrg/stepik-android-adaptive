package org.stepik.android.adaptive.core.presenter

import android.content.Intent
import androidx.annotation.CallSuper
import io.reactivex.Observable
import io.reactivex.Single
import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView
import org.stepik.android.adaptive.util.getPurchasesRx

abstract class PaidContentPresenterBase<V : PaidContentView>(
    private val billing: Billing
) : PresenterBase<V>() {
    protected class PurchasesNotSupportedException : Exception()

    protected var checkout: ActivityCheckout? = null
        private set

    private fun createPurchasesObservable(continuationToken: String? = null) =
        billing.newRequestsBuilder().create().getPurchasesRx(ProductTypes.IN_APP, continuationToken)

    protected fun getAllPurchases(): Observable<Purchase> =
        createPurchasesObservable().concatMap {
            val observable = Observable.just(it)
            if (it.continuationToken != null) {
                observable.concatWith(createPurchasesObservable(it.continuationToken))
            } else {
                observable
            }
        }.concatMap {
            Observable.fromIterable(it.list)
        }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        checkout?.onActivityResult(requestCode, resultCode, data) ?: false

    protected fun getInventory(productType: String, skus: List<String>, callback: (Inventory.Products) -> Unit) {
        val request = Inventory.Request.create()
        request.loadAllPurchases()
        request.loadSkus(productType, skus)
        checkout?.loadInventory(request, callback)
    }

    protected fun getInventoryRx(productType: String, skus: List<String>): Single<List<Sku>> =
        Single.create { emitter ->
            getInventory(productType, skus) { products ->
                val product = products.get(ProductTypes.IN_APP)
                if (product.supported) {
                    emitter.onSuccess(product.skus)
                } else {
                    emitter.onError(PurchasesNotSupportedException())
                }
            }
        }

    @CallSuper
    override fun attachView(view: V) {
        super.attachView(view)
        checkout = view.createCheckout()
        checkout?.start()
    }

    @CallSuper
    override fun detachView(view: V) {
        checkout?.stop()
        checkout = null
        super.detachView(view)
    }
}
