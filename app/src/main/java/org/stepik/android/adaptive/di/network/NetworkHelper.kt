package org.stepik.android.adaptive.di.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkHelper {
    const val TIMEOUT_IN_SECONDS = 60L

    @JvmStatic
    fun createRetrofit(client: OkHttpClient, baseUrl: String, gson: Gson? = null): Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(if (gson != null) GsonConverterFactory.create(gson) else GsonConverterFactory.create())
            .client(client)
            .build()

}