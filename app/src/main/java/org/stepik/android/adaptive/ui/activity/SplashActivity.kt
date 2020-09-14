package org.stepik.android.adaptive.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Singles.zip
import org.json.JSONObject
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.analytics.BranchParams
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    private lateinit var disposable : Disposable

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    @Inject
    lateinit var screenManager: ScreenManager

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

        val authObservable = Single.fromCallable(sharedPreferenceHelper::authResponseDeadline)
        val onboardingObservable = Single.fromCallable(sharedPreferenceHelper::isNotFirstTime)

        disposable = fetchRemoteConfig()
                .andThen(zip(authObservable, onboardingObservable))
                .doOnSuccess { (_, isNotFirstTime) ->
                    val isFirstTime = !isNotFirstTime && !sharedPreferenceHelper.isNotFirstSession
                    if (isFirstTime) {
                        sharedPreferenceHelper.isNotFirstSession = true
                        analytics.logAmplitudeEvent(AmplitudeAnalytics.Launch.FIRST_TIME)
                    }
                }
                .delay(1L, TimeUnit.SECONDS)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe { (authResponseDeadline, isNotFirstTime) ->
                    if (authResponseDeadline != 0L && isNotFirstTime) {
                        screenManager.startStudy()
                    } else {
                        screenManager.showOnboardingScreen()
                    }
                }
    }

    private fun fetchRemoteConfig() = Completable.create { emitter ->
        firebaseRemoteConfig.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseRemoteConfig.activateFetched()
            }
            emitter.onComplete()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onStart() {
        super.onStart()
        intent?.data?.let {
            Branch
                .sessionBuilder(this)
                .withCallback { referringParams, error ->
                    if (error == null && referringParams != null && referringParams.has(BranchParams.FIELD_CAMPAIGN)) {
                        analytics.logAmplitudeEvent(AmplitudeAnalytics.Branch.LINK_OPENED, mapOf(
                                AmplitudeAnalytics.Branch.PARAM_CAMPAIGN to referringParams[BranchParams.FIELD_CAMPAIGN],
                                AmplitudeAnalytics.Branch.IS_FIRST_SESSION to referringParams.optBoolean(BranchParams.IS_FIRST_SESSION, false)
                        ))
                    }
                }
                .withData(it)
                .init()
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}