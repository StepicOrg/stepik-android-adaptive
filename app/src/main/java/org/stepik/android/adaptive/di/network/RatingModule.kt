package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.api.rating.RatingRepository
import org.stepik.android.adaptive.api.rating.RatingRepositoryImpl
import org.stepik.android.adaptive.api.rating.RatingService
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.setTimeoutsInSeconds

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
        internal fun provideRatingService(authInterceptor: AuthInterceptor, config: Config): RatingService =
                NetworkHelper.createServiceWithAuth(authInterceptor, config.ratingHost)
    }
}