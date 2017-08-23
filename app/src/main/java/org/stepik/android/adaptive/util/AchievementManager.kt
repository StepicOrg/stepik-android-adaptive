package org.stepik.android.adaptive.util

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.Presenter
import org.stepik.android.adaptive.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.Achievement
import java.util.*

object AchievementManager : Presenter<AchievementView> {
    private val views = HashSet<AchievementView>()

    val achievements = ArrayList<Achievement>()

    private val queue = ArrayDeque<Achievement>()

    private lateinit var prefix : String

    enum class Event {
        ONBOARDING,
        EXP,
        STREAK,
        DAYS,
        LEVEL
    }

    fun init(context: Context) {
        prefix = context.getString(R.string.ach_prefix)
        initOnboardingAchievement(context)
        initExpAchievements(context)
        initStreakAchievements(context)
        initDaysAchievements(context)
        initLevelAchievements(context)

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
                false
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
                                     @StringRes titlesRes: Int,
                                     @StringRes descriptionRes: Int,
                                     @IntegerRes valuesRes: Int,
                                     @DrawableRes iconsRes: Int) {
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

                    drawables.getResourceId(index, -1))
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

    fun onEvent(event: Event, value: Long, show: Boolean = true) {
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
    fun sync() {
        val exp = ExpUtil.getExp()
        onEvent(Event.EXP, exp, false)
        onEvent(Event.STREAK, ExpUtil.getStreak(), false)
        onEvent(Event.LEVEL, ExpUtil.getCurrentLevel(exp), false)

        DailyRewardManager.syncRewardProgress()
        onEvent(Event.DAYS, DailyRewardManager.totalRewardProgress + 1, false)

        if (SharedPreferenceMgr.getInstance().isNotFirstTime) {
            onEvent(Event.ONBOARDING, 1, false)
        }
    }

    override fun destroy() {}
}