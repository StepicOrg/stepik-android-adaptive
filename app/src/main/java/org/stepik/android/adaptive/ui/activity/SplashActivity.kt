package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    private lateinit var disposable : Disposable

    @Inject
    lateinit var sharedPreferenceMgr: SharedPreferenceMgr

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        setContentView(R.layout.activity_splash)

        val authObservable = Observable.fromCallable(sharedPreferenceMgr::authResponseDeadline)
        val onboardingObservable = Observable.fromCallable(sharedPreferenceMgr::isNotFirstTime)

        disposable = Observable.zip<Long, Boolean, Pair<Long, Boolean>>(authObservable, onboardingObservable, io.reactivex.functions.BiFunction { t1, t2 -> Pair(t1, t2) })
                .delay(1L, TimeUnit.SECONDS)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    if (it.first != 0L && it.second) {
                        ScreenManager.getInstance().startStudy()
                    } else {
                        ScreenManager.getInstance().showOnboardingScreen()
                    }
                })
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}