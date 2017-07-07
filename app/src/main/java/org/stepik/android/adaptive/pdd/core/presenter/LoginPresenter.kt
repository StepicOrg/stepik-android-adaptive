package org.stepik.android.adaptive.pdd.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.pdd.Config
import org.stepik.android.adaptive.pdd.api.API
import org.stepik.android.adaptive.pdd.api.login.SocialManager
import org.stepik.android.adaptive.pdd.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.pdd.data.AnalyticMgr
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

class LoginPresenter : PresenterBase<LoginView>() {
    companion object : PresenterFactory<LoginPresenter> {
        override fun create(): LoginPresenter = LoginPresenter()
    }

    private val disposable = CompositeDisposable()

    private var isSuccess = false

    override fun destroy() {
        disposable.dispose()
    }


    fun authWithLoginPassword(login: String, password: String) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .authWithLoginPassword(login, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onLogin() }, { this.onError() }))
    }

    fun onLogin() {
        disposable.add(API.getInstance()
                .joinCourse(Config.getInstance().courseId)
                .subscribeOn(Schedulers.io())
                .subscribe())

        disposable.add(API.getInstance().profile
                .doOnNext({ SharedPreferenceMgr.getInstance().saveProfile(it.profile) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSuccess() }, { this.onError() }))
    }

    fun onSocialLogin(token: String, type: SocialManager.SocialType) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .authWithNativeCode(token, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onLogin() }, { this.onError() }))
    }

    fun authWithCode(code: String) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .authWithCode(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onLogin() }, { this.onError() }))
    }

    fun onError() {
        view?.onNetworkError()
    }

    fun createAccount(firstName: String, lastName: String, email: String, password: String) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .createAccount(firstName, lastName, email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        authWithLoginPassword(email, password)
                    } else {
                        if (it.errorBody() != null) {
                            view?.onError(it.errorBody().string())
                        } else {
                            view?.onNetworkError()
                        }
                    }
                }, { this.onError() }))
    }


    fun onSuccess() {
        isSuccess = true
        AnalyticMgr.getInstance().successLogin()
        view?.onSuccess()
    }

    override fun attachView(view: LoginView) {
        super.attachView(view)
        if (isSuccess) view.onSuccess()
    }
}