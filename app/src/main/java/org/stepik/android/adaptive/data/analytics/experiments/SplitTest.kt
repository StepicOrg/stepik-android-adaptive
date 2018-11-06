package org.stepik.android.adaptive.data.analytics.experiments

import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.util.random

abstract class SplitTest<G : SplitTest.Group>(
        private val analytics: Analytics,
        private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val SPLIT_TEST_PREFIX = "split_test_"
    }

    abstract val name: String
    protected abstract val groups: Array<G>

    val currentGroup: G
        get() {
            val group = sharedPreferenceHelper
                    .getString(SPLIT_TEST_PREFIX + name)
                    ?.let { groupName -> groups.find { it.name == groupName } }
                    ?: getRandomGroup()

            sharedPreferenceHelper.saveString(SPLIT_TEST_PREFIX + name, group.name)
            analytics.setUserProperty(SPLIT_TEST_PREFIX + name, group.name)

            return group
        }

    private fun getRandomGroup(): G {
        val totalDistribution = groups.map(Group::distribution).sum()

        val seed = (0 until totalDistribution).random()

        var cumulativeSum = 0

        return groups.first {
            cumulativeSum += it.distribution
            cumulativeSum > seed
        }
    }

    interface Group {
        val distribution: Int
        val name: String
    }
}