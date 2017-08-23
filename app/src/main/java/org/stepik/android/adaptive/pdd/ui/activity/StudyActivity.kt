package org.stepik.android.adaptive.pdd.ui.activity

import android.widget.FrameLayout

import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment

import org.stepik.android.adaptive.pdd.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.pdd.data.model.Achievement
import org.stepik.android.adaptive.pdd.ui.animation.AchievementAnimations
import org.stepik.android.adaptive.pdd.util.AchievementManager

class StudyActivity : FragmentActivity(), AchievementView {
    private var isPlayingAchievementAnimation = false

    override fun showAchievement(achievement: Achievement) {
        isPlayingAchievementAnimation = true
        AchievementAnimations.show(findViewById(R.id.fragment_container) as FrameLayout, achievement).withEndAction {
            isPlayingAchievementAnimation = false
            AchievementManager.notifyQueue()
        }
    }

    override fun canShowAchievement() = !isPlayingAchievementAnimation

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
