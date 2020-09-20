package org.stepik.android.adaptive.di.stats

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.activity.StatsActivity
import org.stepik.android.adaptive.ui.dialog.profile.EditEmailDialogFragment
import org.stepik.android.adaptive.ui.dialog.profile.EditNameDialogFragment
import org.stepik.android.adaptive.ui.dialog.profile.EditPasswordDialogFragment
import org.stepik.android.adaptive.ui.fragment.AchievementsFragment
import org.stepik.android.adaptive.ui.fragment.BookmarksFragment
import org.stepik.android.adaptive.ui.fragment.ProfileFragment
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
    fun inject(fragment: ProfileFragment)

    fun inject(activity: StatsActivity)

    fun inject(dialog: EditEmailDialogFragment)
    fun inject(dialog: EditNameDialogFragment)
    fun inject(dialog: EditPasswordDialogFragment)
}
