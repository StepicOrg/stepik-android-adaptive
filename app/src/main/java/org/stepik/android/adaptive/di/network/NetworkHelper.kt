package org.stepik.android.adaptive.di.network

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepik.android.adaptive.api.auth.AuthInterceptor
import org.stepik.android.adaptive.util.setTimeoutsInSeconds
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkHelper {
    const val TIMEOUT_IN_SECONDS = 60L

    @JvmStatic
    fun createRetrofit(client: OkHttpClient, baseUrl: String, gson: Gson = Gson()): Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()


    @JvmStatic
    inline fun <reified T> createService(interceptors: Set<Interceptor>, host: String, gson: Gson = Gson()): T {
        val okHttpBuilder = OkHttpClient.Builder()
        interceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }
        okHttpBuilder.setTimeoutsInSeconds(NetworkHelper.TIMEOUT_IN_SECONDS)
        val retrofit = NetworkHelper.createRetrofit(okHttpBuilder.build(), host, gson)

        return retrofit.create(T::class.java)
    }
}