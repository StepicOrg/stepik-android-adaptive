package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.api.user.UserRepository
import org.stepik.android.adaptive.api.user.UserRepositoryImpl
import org.stepik.android.adaptive.api.user.UserService
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class UserModule {
    @Binds
    @AppSingleton
    abstract fun provideUserRepository(repository: UserRepositoryImpl): UserRepository

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(authInterceptor: AuthInterceptor, config: Config): UserService =
                NetworkHelper.createServiceWithAuth(authInterceptor, config.host)
    }
}