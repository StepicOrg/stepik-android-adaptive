package org.stepik.android.adaptive.pdd.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.core.ScreenManager
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private lateinit var disposable : Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        disposable = Observable.fromCallable(SharedPreferenceMgr.getInstance()::getAuthResponseDeadline)
                .delay(1L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != 0L) {
                        ScreenManager.getInstance().startStudy()
                    } else {
                        ScreenManager.getInstance().showLaunchScreen()
                    }
                })
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}