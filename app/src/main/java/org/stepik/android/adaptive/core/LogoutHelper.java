package org.stepik.android.adaptive.core;

import android.os.Looper;
import android.webkit.CookieManager;

import com.vk.sdk.VKSdk;

import org.stepik.android.adaptive.Util;
import org.stepik.android.adaptive.data.SharedPreferenceMgr;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class LogoutHelper {
    public static void logout(Action onComplete) {
        Completable c = Completable
                .fromRunnable(() -> {
                    removeCookiesCompat();
                    VKSdk.logout();
                    SharedPreferenceMgr.getInstance().removeProfile();
//                    ExpManager.reset();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        if (onComplete != null) {
            c.subscribe(onComplete);
        } else {
            c.subscribe();
        }
    }

    public static void removeCookiesCompat() {
        if (Util.isLowAndroidVersion()) {
            CookieManager.getInstance().removeAllCookie();
        } else {
            Completable.fromRunnable(() -> {
                Looper.prepare();
                CookieManager.getInstance().removeAllCookies((__) -> {});
                Looper.loop();
            }).subscribeOn(Schedulers.io()).subscribe();
        }
    }
}
