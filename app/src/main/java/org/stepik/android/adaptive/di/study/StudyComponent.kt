package org.stepik.android.adaptive.di.study

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.activity.StudyActivity
import org.stepik.android.adaptive.ui.fragment.OnboardingFragment
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment

@Subcomponent
interface StudyComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): StudyComponent
    }

    fun inject(studyActivity: StudyActivity)

    fun inject(recommendationsFragment: RecommendationsFragment)
    fun inject(onboardingFragment: OnboardingFragment)

}