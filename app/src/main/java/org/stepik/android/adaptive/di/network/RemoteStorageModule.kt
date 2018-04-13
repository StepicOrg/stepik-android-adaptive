package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.api.storage.RemoteStorageRepository
import org.stepik.android.adaptive.api.storage.RemoteStorageRepositoryImpl
import org.stepik.android.adaptive.api.storage.RemoteStorageService
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class RemoteStorageModule {
    @Binds
    @AppSingleton
    abstract fun provideAuthRepository(authRepositoryImpl: RemoteStorageRepositoryImpl): RemoteStorageRepository

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(authInterceptor: AuthInterceptor, config: Config): RemoteStorageService =
                NetworkHelper.createServiceWithAuth(authInterceptor, config.host)
    }
}