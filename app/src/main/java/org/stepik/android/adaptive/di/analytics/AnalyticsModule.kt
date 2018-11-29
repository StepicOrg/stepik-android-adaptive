package org.stepik.android.adaptive.di.analytics

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.analytics.AnalyticsImpl

@Module
abstract class AnalyticsModule {
    @Binds
    internal abstract fun bindAnalytics(analyticsImpl: AnalyticsImpl): Analytics
}