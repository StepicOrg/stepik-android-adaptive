package org.stepik.android.adaptive.arch.data.billing.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.arch.data.billing.source.BillingRemoteDataSource
import org.stepik.android.adaptive.arch.domain.billing.repository.BillingRepository
import javax.inject.Inject

class BillingRepositoryImpl
@Inject
constructor(
    private val billingRemoteDataSource: BillingRemoteDataSource
) : BillingRepository {
    override fun getInventory(productType: String, skuIds: List<String>): Single<List<Sku>> =
        billingRemoteDataSource.getInventory(productType, skuIds)

    override fun getAllPurchases(productType: String): Single<List<Purchase>> =
        billingRemoteDataSource.getAllPurchases(productType)

    override fun consumePurchase(purchase: Purchase): Completable =
        billingRemoteDataSource.consumePurchase(purchase)
}