package org.stepik.android.adaptive.di.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.api.StepikService
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.model.DatasetWrapper
import org.stepik.android.adaptive.data.preference.AuthPreferences
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.json.DatasetWrapperDeserializer

@Module(includes = [AuthModule::class, ProfileModule::class, RatingModule::class, RemoteStorageModule::class])
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
        internal fun provideModelGson(): Gson = GsonBuilder()
                .registerTypeAdapter(DatasetWrapper::class.java, DatasetWrapperDeserializer())
                .create()

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(authInterceptor: AuthInterceptor, config: Config, gson: Gson): StepikService =
                NetworkHelper.createServiceWithAuth(authInterceptor, config.host, gson)
    }

}