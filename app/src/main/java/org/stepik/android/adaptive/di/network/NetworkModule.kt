package org.stepik.android.adaptive.di.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.stepik.android.adaptive.api.StepikService
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.model.DatasetWrapper
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.json.DatasetWrapperDeserializer
import org.stepik.android.adaptive.util.setTimeoutsInSeconds

@Module(includes = [AuthModule::class, RatingModule::class, RemoteStorageModule::class])
abstract class NetworkModule {

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(authInterceptor: AuthInterceptor, config: Config): StepikService {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.addInterceptor(authInterceptor)

            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(logger)

            val gson = GsonBuilder()
                    .registerTypeAdapter(DatasetWrapper::class.java, DatasetWrapperDeserializer())
                    .create()


            okHttpBuilder.setTimeoutsInSeconds(NetworkHelper.TIMEOUT_IN_SECONDS)
            val retrofit = NetworkHelper.createRetrofit(okHttpBuilder.build(), config.host, gson)

            return retrofit.create(StepikService::class.java)
        }
    }

}