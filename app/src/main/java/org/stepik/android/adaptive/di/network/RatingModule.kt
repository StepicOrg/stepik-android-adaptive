package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.api.rating.RatingRepository
import org.stepik.android.adaptive.api.rating.RatingRepositoryImpl
import org.stepik.android.adaptive.api.rating.RatingService
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class RatingModule {
    @Binds
    @AppSingleton
    abstract fun provideAuthRepository(ratingRepositoryImpl: RatingRepositoryImpl): RatingRepository

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideRatingService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): RatingService =
                NetworkHelper.createService(interceptors, config.ratingHost)
    }
}