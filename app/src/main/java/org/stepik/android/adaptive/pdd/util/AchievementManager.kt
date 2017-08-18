package org.stepik.android.adaptive.pdd.util

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.core.presenter.Presenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.AchievementView
import org.stepik.android.adaptive.pdd.data.model.Achievement
import java.util.concurrent.TimeUnit

object AchievementManager : Presenter<AchievementView> {
    private val views = HashSet<AchievementView>()

    private val achievements = ArrayList<Achievement>()

    private lateinit var prefix : String

    fun init(context: Context) {
        prefix = context.getString(R.string.ach_prefix)
        initOnboardingAchievement(context)
        initExpAchievements(context)
        initStreakAchievements(context)
        initDaysAchievements(context)
        initLevelAchievements(context)
    }

    private fun initOnboardingAchievement(context: Context) {
        achievements.add(Achievement(
                context.getString(R.string.ach_onboarding_title),
                context.getString(R.string.ach_onboarding_description),
                prefix + context.getString(R.string.ach_onboarding_prefix),
                context.getString(R.string.ach_onboarding_event),
                -1
        ))
    }

    private fun initExpAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_exp_prefix,
                R.string.ach_exp_event,
                R.array.ach_exp_titles,
                R.string.ach_exp_description,
                R.array.ach_exp_values,
                -1)
    }

    private fun initStreakAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_streak_prefix,
                R.string.ach_streak_event,
                R.array.ach_streak_titles,
                R.string.ach_streak_description,
                R.array.ach_streak_values,
                -1)
    }

    private fun initDaysAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_days_prefix,
                R.string.ach_days_event,
                R.array.ach_days_titles,
                R.string.ach_days_description,
                R.array.ach_days_values,
                -1)
    }

    private fun initLevelAchievements(context: Context) {
        initAchievementGroup(context,
                R.string.ach_level_prefix,
                R.string.ach_level_event,
                R.array.ach_level_titles,
                R.string.ach_level_description,
                R.array.ach_level_values,
                -1)
    }

    private fun initAchievementGroup(context: Context,
                                     @StringRes typePrefixRes: Int,
                                     @StringRes eventTypeRes: Int,
                                     @StringRes titlesRes: Int,
                                     @StringRes descriptionRes: Int,
                                     @IntegerRes valuesRes: Int,
                                     @DrawableRes iconsRes: Int) {
        val titles = context.resources.getStringArray(titlesRes)
        val values = context.resources.getIntArray(valuesRes)
        val event = context.getString(eventTypeRes)

//        val drawables = context.resources.obtainTypedArray(iconsRes)

        achievements.addAll(titles.mapIndexed { index, title ->
            Achievement(
                    title,
                    context.getString(descriptionRes, values[index]),
                    prefix + context.getString(typePrefixRes, values[index]),
                    event,
                    -1) //drawables.getResourceId(index, -1))
        })

//        drawables.recycle()
    }

    override fun attachView(view: AchievementView) {
        views.add(view)

        Completable.complete()
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onAchievement(achievements[Util.getRandomNumberBetween(0, achievements.size - 1)]) }

    }

    override fun detachView(view: AchievementView) {
        views.remove(view)
    }

    private fun onAchievement(achievement: Achievement) {
        views.forEach { it.showAchievement(achievement) }
    }


    override fun destroy() {}
}