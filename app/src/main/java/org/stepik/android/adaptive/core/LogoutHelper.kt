package org.stepik.android.adaptive.core

import com.vk.sdk.VKSdk

import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton

import io.reactivex.Completable
import io.reactivex.Scheduler
import org.stepik.android.adaptive.api.auth.CookieHelper
import org.stepik.android.adaptive.di.qualifiers.AuthLock
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@AppSingleton
class LogoutHelper
@Inject
constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,

        @AuthLock
        private val authLock: ReentrantLock,

        private val cookieHelper: CookieHelper
) {

    fun logout(onComplete: (() -> Unit)?) {
        val c = Completable.fromRunnable {
                    cookieHelper.removeCookiesCompat()
                    VKSdk.logout()

                    authLock.withLock {
                        sharedPreferenceHelper.removeProfile()
                    }
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
}
