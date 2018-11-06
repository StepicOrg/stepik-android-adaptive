package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.api.profile.ProfileRepositoryImpl
import org.stepik.android.adaptive.api.profile.ProfileService
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class ProfileModule {
    @Binds
    @AppSingleton
    abstract fun provideAuthRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideProfileService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): ProfileService =
                NetworkHelper.createService(interceptors, config.host)
    }
}