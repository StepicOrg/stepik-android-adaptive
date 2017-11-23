package org.stepik.android.adaptive.ui.activity

import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.ui.fragment.CardsFragment

import org.stepik.android.adaptive.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.data.model.Achievement
import org.stepik.android.adaptive.ui.animation.AchievementAnimations
import org.stepik.android.adaptive.util.AchievementManager

class StudyActivity : FragmentActivity(), AchievementView {
    private var isPlayingAchievementAnimation = false

    override fun showAchievement(achievement: Achievement) {
        isPlayingAchievementAnimation = true
        AchievementAnimations.show(findViewById(R.id.fragment_container), achievement).withEndAction {
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
