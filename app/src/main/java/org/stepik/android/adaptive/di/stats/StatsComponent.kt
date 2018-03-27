package org.stepik.android.adaptive.di.stats

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.activity.StatsActivity
import org.stepik.android.adaptive.ui.fragment.AchievementsFragment
import org.stepik.android.adaptive.ui.fragment.BookmarksFragment
import org.stepik.android.adaptive.ui.fragment.ProgressFragment
import org.stepik.android.adaptive.ui.fragment.RatingFragment

@Subcomponent
interface StatsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): StatsComponent
    }

    fun inject(fragment: ProgressFragment)
    fun inject(fragment: AchievementsFragment)
    fun inject(fragment: RatingFragment)
    fun inject(fragment: BookmarksFragment)

    fun inject(activity: StatsActivity)

}