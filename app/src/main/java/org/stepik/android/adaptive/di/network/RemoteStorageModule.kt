package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.api.storage.RemoteStorageRepository
import org.stepik.android.adaptive.api.storage.RemoteStorageRepositoryImpl
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class RemoteStorageModule {
    @Binds
    @AppSingleton
    abstract fun provideAuthRepository(authRepositoryImpl: RemoteStorageRepositoryImpl): RemoteStorageRepository
}