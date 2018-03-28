package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.login.SocialManager
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.addDisposable
import retrofit2.HttpException
import javax.inject.Inject

class LoginPresenter
@Inject
constructor(
        private val api: Api,
        private val config: Config,
        private val sharedPreferenceMgr: SharedPreferenceMgr,
        private val analytics: Analytics,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<LoginView>() {

    private val disposable = CompositeDisposable()

    private var isSuccess = false

    override fun destroy() {
        disposable.dispose()
    }

    fun authWithLoginPassword(login: String, password: String, isFake: Boolean = false) {
        view?.onLoading()

        disposable.add(api
                .authWithLoginPassword(login, password)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
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
        disposable addDisposable api
                .joinCourse(config.courseId)
                .andThen(api.profile)
                .doOnNext { sharedPreferenceMgr.profile = it.profile }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    if (isFake)
                        unsubscribeFake(it.profile)
                    else
                        onSuccess()
                }, { this.onError() })
    }

    private fun unsubscribeFake(profile: Profile) {
        profile.setSubscribed_for_mail(false)
        disposable addDisposable api.setProfile(profile)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe(this::onSuccess, { this.onError() })
    }

    fun onSocialLogin(token: String, type: SocialManager.SocialType) {
        view?.onLoading()

        disposable addDisposable api
                .authWithNativeCode(token, type)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({ this.onLogin() }, { this.onError() })
    }

    fun authWithCode(code: String) {
        view?.onLoading()

        disposable addDisposable api
                .authWithCode(code)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({ this.onLogin() }, { this.onError() })
    }

    fun onError() {
        view?.onNetworkError()
    }

    fun createFakeUser() = createAccount(api.createFakeAccount(), true)

    fun createAccount(credentials: AccountCredentials, isFake: Boolean = false) {
        view?.onLoading()

        disposable addDisposable api
                .createAccount(credentials.firstName, credentials.lastName, credentials.login, credentials.password)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    if (it.isSuccessful) {
                        if (isFake) { // save only if it's fake account
                            sharedPreferenceMgr.saveFakeUser(credentials)
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
                }, { this.onError() })
    }


    fun onSuccess() {
        isSuccess = true
        analytics.successLogin()
        view?.onSuccess()
    }

    override fun attachView(view: LoginView) {
        super.attachView(view)
        if (isSuccess) view.onSuccess()
    }
}