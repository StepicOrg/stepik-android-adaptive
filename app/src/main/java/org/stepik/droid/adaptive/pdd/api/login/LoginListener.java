package org.stepik.droid.adaptive.pdd.api.login;

import org.stepik.droid.adaptive.pdd.api.API;
import org.stepik.droid.adaptive.pdd.api.oauth.OAuthResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class LoginListener {
    private Disposable disposable;

    public abstract void onLogin(final OAuthResponse response);
    public void onSocialLogin(final String token, final SocialManager.SocialType type) {
        if (disposable == null || disposable.isDisposed()) {
            disposable = API.getInstance()
                    .authWithNativeCode(token, type)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(API.getInstance()::updateAuthState)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onLogin, this::onError);
        }
    }
    public abstract void onError(final Throwable throwable);
}
