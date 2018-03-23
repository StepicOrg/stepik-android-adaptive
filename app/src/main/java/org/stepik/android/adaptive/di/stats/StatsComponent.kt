package org.stepik.android.adaptive.di.stats

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.fragment.ProgressFragment

@Subcomponent
interface StatsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): StatsComponent
    }

    fun inject(fragment: ProgressFragment)

}