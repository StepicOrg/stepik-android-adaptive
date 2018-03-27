package org.stepik.android.adaptive.core

import android.os.Looper
import android.webkit.CookieManager

import com.vk.sdk.VKSdk

import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.di.AppSingleton

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.functions.Action
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import javax.inject.Inject

@AppSingleton
class LogoutHelper
@Inject
constructor(
        private val sharedPreferenceMgr: SharedPreferenceMgr,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) {

    fun logout(onComplete: (() -> Unit)?) {
        val c = Completable
                .fromRunnable {
                    removeCookiesCompat()
                    VKSdk.logout()
                    sharedPreferenceMgr.removeProfile()
                    //                    ExpManager.reset();
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)

        if (onComplete != null) {
            c.subscribe(onComplete)
        } else {
            c.subscribe()
        }
    }

    fun removeCookiesCompat() {
        if (Util.isLowAndroidVersion()) {
            CookieManager.getInstance().removeAllCookie()
        } else {
            Completable.fromRunnable {
                Looper.prepare()
                CookieManager.getInstance().removeAllCookies(null)
                Looper.loop()
            }.subscribeOn(backgroundScheduler).subscribe()
        }
    }
}
