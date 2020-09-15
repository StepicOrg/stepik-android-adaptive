package org.stepik.android.adaptive.ui.activity

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_activity.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.core.ScreenManager

import org.stepik.android.adaptive.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.data.model.Achievement
import org.stepik.android.adaptive.ui.animation.AchievementAnimations
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import javax.inject.Inject

class StudyActivity : FragmentActivity(), AchievementView {
    private var isPlayingAchievementAnimation = false

    @Inject
    lateinit var achievementManager: AchievementManager

    @Inject
    lateinit var screenManager: ScreenManager

    override fun injectComponent() {
        App.componentManager()
                .studyComponent
                .inject(this)
    }

    override fun showAchievement(achievement: Achievement) {
        isPlayingAchievementAnimation = true
        AchievementAnimations.show(fragment_container, achievement, screenManager).withEndAction {
            isPlayingAchievementAnimation = false
            achievementManager.notifyQueue()
        }
    }

    override fun canShowAchievement() = !isPlayingAchievementAnimation

    override fun onStart() {
        super.onStart()
        achievementManager.attachView(this)
    }

    override fun onStop() {
        achievementManager.detachView(this)
        super.onStop()
    }

    override fun createFragment(): Fragment = RecommendationsFragment()
}
