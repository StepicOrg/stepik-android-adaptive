package org.stepik.android.adaptive.data.analytics.experiments

import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import javax.inject.Inject


class QuestionPackPricesDiscountSplitTest
@Inject
constructor(
        analytics: Analytics,
        sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<QuestionPackPricesDiscountSplitTest.Group>(analytics, sharedPreferenceHelper) {
    override val name: String = "question_pack_price_discount"
    override val groups: Array<Group> = Group.values()

    enum class Group(
            val displayPriceMultiplier: Double
    ) : SplitTest.Group {
        Control(1.0),
        Test20PercentDiscount(0.8);

        override val distribution: Int = 1
    }
}