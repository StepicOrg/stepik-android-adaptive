package org.stepik.android.adaptive.arch.view.injection.billing

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.arch.data.billing.repository.BillingRepositoryImpl
import org.stepik.android.adaptive.arch.data.billing.source.BillingRemoteDataSource
import org.stepik.android.adaptive.arch.domain.billing.repository.BillingRepository
import org.stepik.android.adaptive.arch.remote.billing.BillingRemoteDataSourceImpl

@Module
abstract class BillingDataModule {
    @Binds
    internal abstract fun bindBillingRepository(billingRepositoryImpl: BillingRepositoryImpl): BillingRepository

    @Binds
    internal abstract fun bindBillingRemoteDataSource(billingRemoteDataSourceImpl: BillingRemoteDataSourceImpl): BillingRemoteDataSource
}
