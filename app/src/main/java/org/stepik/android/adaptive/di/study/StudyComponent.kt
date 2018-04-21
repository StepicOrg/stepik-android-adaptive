package org.stepik.android.adaptive.di.study

import dagger.Subcomponent
import org.stepik.android.adaptive.core.presenter.CardPresenter
import org.stepik.android.adaptive.data.model.Card
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.ui.activity.DescriptionActivity
import org.stepik.android.adaptive.ui.activity.StudyActivity
import org.stepik.android.adaptive.ui.fragment.OnboardingFragment
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment

@StudyScope
@Subcomponent(modules = [StudyModule::class])
interface StudyComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): StudyComponent
    }

    fun inject(studyActivity: StudyActivity)
    fun inject(descriptionActivity: DescriptionActivity)

    fun inject(recommendationsFragment: RecommendationsFragment)
    fun inject(onboardingFragment: OnboardingFragment)

    fun inject(cardPresenter: CardPresenter)
    fun inject(card: Card)

}