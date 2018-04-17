package org.stepik.android.adaptive.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.api.StepikService
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.preference.AuthPreferences
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton

@Module(includes = [AuthModule::class, ProfileModule::class, RatingModule::class, RemoteStorageModule::class, UserModule::class])
abstract class NetworkModule {

    // AuthModule link
    @Binds
    @AppSingleton
    abstract fun provideAuthPreferences(sharedPreferenceHelper: SharedPreferenceHelper): AuthPreferences

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(authInterceptor: AuthInterceptor, config: Config): StepikService =
                NetworkHelper.createServiceWithAuth(authInterceptor, config.host)
    }

}