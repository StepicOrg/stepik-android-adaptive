package org.stepik.android.adaptive.di.analytics

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.analytics.AnalyticsImpl
import org.stepik.android.adaptive.data.analytics.experiments.QuestionPackPricesDiscountSplitTest
import org.stepik.android.adaptive.data.analytics.experiments.SplitTest

@Module
abstract class AnalyticsModule {
    @Binds
    internal abstract fun bindAnalytics(analyticsImpl: AnalyticsImpl): Analytics

    @Binds
    @IntoSet
    internal abstract fun bindQuestionPackPricesDiscountSplitTest(
            questionPackPricesDiscountSplitTest: QuestionPackPricesDiscountSplitTest): SplitTest<*>
}