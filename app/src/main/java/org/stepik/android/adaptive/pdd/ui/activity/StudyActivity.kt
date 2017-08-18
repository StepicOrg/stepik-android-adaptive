package org.stepik.android.adaptive.pdd.ui.activity

import android.widget.FrameLayout

import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment

import org.stepik.android.adaptive.pdd.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.pdd.data.model.Achievement
import org.stepik.android.adaptive.pdd.ui.animation.AchievementAnimations
import org.stepik.android.adaptive.pdd.util.AchievementManager

class StudyActivity : FragmentActivity(), AchievementView {
    override fun showAchievement(achievement: Achievement) =
        AchievementAnimations.show(findViewById(R.id.fragment_container) as FrameLayout, achievement)

    override fun onStart() {
        super.onStart()
        AchievementManager.attachView(this)
    }

    override fun onStop() {
        AchievementManager.detachView(this)
        super.onStop()
    }

    override fun createFragment() = CardsFragment()
}
