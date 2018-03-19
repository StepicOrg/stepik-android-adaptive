package org.stepik.android.adaptive.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.api.API
import org.stepik.android.adaptive.api.login.SocialManager
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.data.AnalyticMgr
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import retrofit2.HttpException

class LoginPresenter : PresenterBase<LoginView>() {
    companion object : PresenterFactory<LoginPresenter> {
        override fun create(): LoginPresenter = LoginPresenter()
    }

    private val disposable = CompositeDisposable()

    private var isSuccess = false

    override fun destroy() {
        disposable.dispose()
    }

    fun authWithLoginPassword(login: String, password: String, isFake: Boolean = false) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .authWithLoginPassword(login, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onLogin(isFake) }, {
                    if (isFake && it is HttpException && it.code() == 401) {
                        // on some reason we can't login for fake user, so recreate fake user
                        createFakeUser()
                    } else {
                        this.onError()
                    }
                }))
    }

    fun onLogin(isFake: Boolean = false) {
        disposable.add(API.getInstance()
                .joinCourse(Config.getInstance().courseId)
                .andThen(API.getInstance().profile)
                .doOnNext({ SharedPreferenceMgr.getInstance().saveProfile(it.profile) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (isFake)
                        unsubscribeFake(it.profile)
                    else
                        onSuccess()
                }, { this.onError() }))
    }

    private fun unsubscribeFake(profile: Profile) {
        profile.setSubscribed_for_mail(false)
        disposable.add(API.getInstance().setProfile(profile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, { this.onError() }))
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

    fun createFakeUser() = createAccount(API.createFakeAccount(), true)

    fun createAccount(credentials: AccountCredentials, isFake: Boolean = false) {
        view?.onLoading()

        disposable.add(API.getInstance()
                .createAccount(credentials.firstName, credentials.lastName, credentials.login, credentials.password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful) {
                        if (isFake) { // save only if it's fake account
                            SharedPreferenceMgr.getInstance().saveFakeUser(credentials)
                        }
                        authWithLoginPassword(credentials.login, credentials.password, isFake)
                    } else {
                        val errorBody = it.errorBody()
                        if (errorBody != null) {
                            view?.onError(errorBody.string())
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