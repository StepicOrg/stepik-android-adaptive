package org.stepik.android.adaptive.util

import io.reactivex.Completable
import io.reactivex.Observable
import org.solovyev.android.checkout.*

fun BillingRequests.getPurchasesRx(type: String, continuationToken: String?): Observable<Purchases> = Observable.create { emitter ->
    getPurchases(type, continuationToken, object : RequestListener<Purchases> {
        override fun onSuccess(purchases: Purchases) {
            emitter.onNext(purchases)
            emitter.onComplete()
        }

        override fun onError(response: Int, exception: Exception) {
            emitter.onError(exception)
        }
    })
}


fun Checkout.onReady(): Observable<BillingRequests> = Observable.create { emitter ->
    whenReady(object : Checkout.EmptyListener() {
        override fun onReady(requests: BillingRequests) {
            emitter.onNext(requests)
            emitter.onComplete()
        }
    })
}

fun BillingRequests.consumeRx(token: String): Completable = Completable.create { emitter ->
    consume(token, object : RequestListener<Any> {
        override fun onSuccess(result: Any) {
            emitter.onComplete()
        }

        override fun onError(response: Int, exception: Exception) {
            emitter.onError(exception)
        }
    })
}

fun ActivityCheckout.startPurchaseFlowRx(sku: Sku): Observable<Purchase> = Observable.create { emitter ->
    startPurchaseFlow(sku, null, object : RequestListener<Purchase> {
        override fun onSuccess(purchase: Purchase) {
            emitter.onNext(purchase)
            emitter.onComplete()
        }

        override fun onError(response: Int, exception: Exception) {
            emitter.onError(exception)
        }
    })
}