package org.stepik.android.adaptive.gamification.achievements

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.events.Client
import org.stepik.android.adaptive.core.presenter.Presenter
import org.stepik.android.adaptive.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.Achievement
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.DailyRewardManager
import org.stepik.android.adaptive.gamification.ExpManager
import java.util.*
import javax.inject.Inject

@AppSingleton
class AchievementManager
@Inject
constructor(
        context: Context,
        private val expManager: ExpManager,
        private val dailyRewardManager: DailyRewardManager,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        eventClient: Client<AchievementEventListener>
): Presenter<AchievementView>, AchievementEventListener {
    private val views = HashSet<AchievementView>()

    val achievements = ArrayList<Achievement>()

    private val queue = ArrayDeque<Achievement>()
    private val prefix = context.getString(R.string.ach_prefix)

    enum class Event {
        ONBOARDING,
        EXP,
        STREAK,
        DAYS,
        LEVEL
    }

    init {
        initOnboardingAchievement(context)
        initExpAchievements(context)
        initStreakAchievements(context)
        initDaysAchievements(context)
        initLevelAchievements(context)

        eventClient.subscribe(this)

        sync()
    }

    private fun initOnboardingAchievement(context: Context) {
        achievements.add(Achievement(
                context.getString(R.string.ach_onboarding_title),
                context.getString(R.string.ach_onboarding_description),
                prefix + context.getString(R.string.ach_onboarding_prefix),
                Event.ONBOARDING,
                1,
                R.drawable.ic_ach_onboarding,
                false,
                sharedPreferenceHelper
        ))
    }

    private fun initExpAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_exp_prefix,
                Event.EXP,
                R.array.ach_exp_titles,
                R.string.ach_exp_description,
                R.array.ach_exp_values,
                R.array.ach_exp_icons)
    }

    private fun initStreakAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_streak_prefix,
                Event.STREAK,
                R.array.ach_streak_titles,
                R.string.ach_streak_description,
                R.array.ach_streak_values,
                R.array.ach_streak_icons)
    }

    private fun initDaysAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_days_prefix,
                Event.DAYS,
                R.array.ach_days_titles,
                R.string.ach_days_description,
                R.array.ach_days_values,
                R.array.ach_days_icons)
    }

    private fun initLevelAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_level_prefix,
                Event.LEVEL,
                R.array.ach_level_titles,
                R.string.ach_level_description,
                R.array.ach_level_values,
                R.array.ach_level_icons)
    }

    private fun initAchievementGroup(context: Context,
                                     @StringRes typePrefixRes: Int,
                                     event: Event,
                                     @ArrayRes titlesRes: Int,
                                     @StringRes descriptionRes: Int,
                                     @ArrayRes valuesRes: Int,
                                     @ArrayRes iconsRes: Int) {
        val titles = context.resources.getStringArray(titlesRes)
        val values = context.resources.getIntArray(valuesRes)

        val drawables = context.resources.obtainTypedArray(iconsRes)

        achievements.addAll(titles.mapIndexed { index, title ->
            Achievement(
                    title,
                    context.getString(descriptionRes, values[index]),
                    prefix + context.getString(typePrefixRes, values[index]),
                    event,
                    values[index].toLong(),

                    drawables.getResourceId(index, -1),
                    sharedPreferenceHelper = sharedPreferenceHelper)
        })

        drawables.recycle()
    }

    override fun attachView(view: AchievementView) {
        views.add(view)
        notifyQueue()
    }

    override fun detachView(view: AchievementView) {
        views.remove(view)
    }

    fun notifyQueue() {
        if (queue.isNotEmpty() && views.isNotEmpty() && views.all { it.canShowAchievement() })
            onAchievement(queue.poll())
    }

    private fun onAchievement(achievement: Achievement) {
        views.forEach { it.showAchievement(achievement) }
    }

    override fun onEvent(event: Event, value: Long, show: Boolean) {
        achievements.filter { it.eventType == event && !it.isComplete() }.forEach {
            it.currentValue = Math.min(it.targetValue, Math.max(value, it.currentValue))
            if (it.isComplete() && show) {
                queue.add(it)
                notifyQueue()
            }
        }
    }


    /**
     * Method to sync achievements and stats
     */
    private fun sync() {
        val exp = expManager.exp
        onEvent(Event.EXP, exp, false)
        onEvent(Event.STREAK, expManager.streak, false)
        onEvent(Event.LEVEL, expManager.getCurrentLevel(exp), false)

        dailyRewardManager.syncRewardProgress()
        onEvent(Event.DAYS, dailyRewardManager.totalRewardProgress + 1, false)

        if (sharedPreferenceHelper.isNotFirstTime) {
            onEvent(Event.ONBOARDING, 1, false)
        }
    }

    override fun destroy() {}
}