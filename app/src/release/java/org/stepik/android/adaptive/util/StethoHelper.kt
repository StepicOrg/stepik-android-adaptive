package org.stepik.android.adaptive.util

import android.app.Application
import okhttp3.Interceptor

object StethoHelper {
    fun initStetho(app: Application) {
        // no op
    }

    fun getInterceptor(): Interceptor =
            Interceptor { it.proceed(it.request()) }
}