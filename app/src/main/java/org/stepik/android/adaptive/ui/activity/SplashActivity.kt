package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    private lateinit var disposable : Disposable

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

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

        val authObservable = Observable.fromCallable(sharedPreferenceHelper::authResponseDeadline)
        val onboardingObservable = Observable.fromCallable(sharedPreferenceHelper::isNotFirstTime)

        disposable = fetchRemoteConfig().andThen(Observable.zip<Long, Boolean, Pair<Long, Boolean>>(authObservable, onboardingObservable, BiFunction { t1, t2 -> Pair(t1, t2) }))
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

    private fun fetchRemoteConfig() = Completable.create { emitter ->
        firebaseRemoteConfig.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseRemoteConfig.activateFetched()
            }
            emitter.onComplete()
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}