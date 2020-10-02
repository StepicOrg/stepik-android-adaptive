package org.stepik.android.adaptive.util

import android.app.Application
import okhttp3.Interceptor

object DebugToolsHelper {
    fun initDebugTools(app: Application) {
        // no op
    }

    fun getDebugInterceptors(): Set<Interceptor> =
        emptySet()
}