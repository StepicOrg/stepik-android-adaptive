package org.stepik.android.adaptive.gamification

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.rxkotlin.plusAssign
import org.stepik.android.adaptive.api.rating.RatingRepository
import org.stepik.android.adaptive.configuration.RemoteConfig
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.gamification.achievements.AchievementEventPoster
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import org.stepik.android.adaptive.util.then
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

@AppSingleton
class ExpManager
@Inject
constructor(
    private val ratingRepository: RatingRepository,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    private val achievementEventPoster: AchievementEventPoster,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val dataBaseMgr: DataBaseMgr,
    private val analytics: Analytics,
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    companion object {
        private const val EXP_KEY = "exp_key"
        private const val STREAK_KEY = "streak_key"

        private const val LEVEL_POW = 2.5

        fun syncRating(dataBaseMgr: DataBaseMgr, ratingRepository: RatingRepository): Completable =
            dataBaseMgr.getExp().flatMapCompletable { e -> ratingRepository.putRating(e) }
    }

    private val compositeDisposable = CompositeDisposable()

    var exp: Long
        get() = sharedPreferenceHelper.getLong(EXP_KEY)
        private set(value) = sharedPreferenceHelper.saveLong(EXP_KEY, value)

    val streak: Long
        get() = sharedPreferenceHelper.getLong(STREAK_KEY)

    fun fetchExp(): Observable<Long> =
        Observable.concat(
            Observable.just(exp),
            dataBaseMgr.getExp().toObservable(),
            ratingRepository.fetchRating().flatMap { dataBaseMgr.syncExp(it) }.toObservable()
        ).doOnNext {
            exp = it
        }.onErrorReturn {
            exp
        }

    fun changeExp(delta: Long, submissionId: Long): Long {
        val exp = sharedPreferenceHelper.changeLong(EXP_KEY, delta)
        analytics.onExpReached(exp - delta, delta)
        analytics.setUserExp(exp)

        achievementEventPoster.onEvent(AchievementManager.Event.EXP, exp, true)

        compositeDisposable += (dataBaseMgr.onExpGained(delta, submissionId) then syncRating(dataBaseMgr, ratingRepository))
            .subscribeOn(backgroundScheduler)
            .subscribe(
                Functions.EMPTY_ACTION,
                { e ->
                    if (e is HttpException) {
                        analytics.onRatingError()
                    }
                }
            )

        return exp
    }

    fun incStreak(): Long =
        changeStreak(1)

    fun changeStreak(delta: Long): Long {
        val streak = sharedPreferenceHelper.changeLong(STREAK_KEY, delta)
        analytics.onStreak(streak)
        achievementEventPoster.onEvent(AchievementManager.Event.STREAK, streak, true)
        return streak
    }

    fun getCurrentLevel(exp: Long): Long {
        val level = if (firebaseRemoteConfig.getBoolean(RemoteConfig.EXP_LEVEL_FORMULA_EXPERIMENT)) {
            exp.toDouble().pow(1.0 / LEVEL_POW).toLong()
        } else {
            if (exp < 5) return 1
            2 + (ln((exp / 5).toDouble()) / ln(2.0)).toLong()
        }

        analytics.setUserLevel(level)
        achievementEventPoster.onEvent(AchievementManager.Event.LEVEL, level, true)

        return level
    }

    fun getNextLevelExp(currentLevel: Long): Long =
        if (firebaseRemoteConfig.getBoolean(RemoteConfig.EXP_LEVEL_FORMULA_EXPERIMENT)) {
            ceil((currentLevel.toDouble() + 1).pow(LEVEL_POW)).toLong()
        } else {
            if (currentLevel == 1L) 5 else 5 * 2.0.pow((currentLevel - 1).toDouble()).toLong()
        }

    fun resetStreak() {
        analytics.onStreakLost(streak)
        sharedPreferenceHelper.saveLong(STREAK_KEY, 0)
    }

    fun reset(): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.remove(EXP_KEY)
            sharedPreferenceHelper.remove(STREAK_KEY)
        } then dataBaseMgr.resetExp()
}
