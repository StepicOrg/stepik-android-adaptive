package org.stepik.android.adaptive.core.presenter

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.auth.AuthError
import org.stepik.android.adaptive.api.auth.AuthRepository
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.presenter.contracts.AuthView
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.ExpManager
import org.stepik.android.adaptive.util.RxOptional
import org.stepik.android.adaptive.util.addDisposable
import org.stepik.android.adaptive.util.then
import retrofit2.HttpException
import javax.inject.Inject

class AuthPresenter
@Inject
constructor(
        private val api: Api,
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
        private val profilePreferences: ProfilePreferences,
        private val analytics: Analytics,

        private val questionsPacksManager: QuestionsPacksManager,
        private val expManager: ExpManager,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<AuthView>() {

    private val disposable = CompositeDisposable()

    private var isSuccess = false

    fun authFakeUser() {
        view?.onLoading()

        disposable addDisposable createFakeUserRx()
                .andThen(onLoginRx())
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe(this::onSuccess, {
                    onError(AuthError.ConnectionProblem)
                })
    }

    fun authWithLoginPassword(login: String, password: String) {
        view?.onLoading()

        disposable addDisposable loginRx(login, password).andThen(onLoginRx())
                .doOnComplete {
                    profilePreferences.removeFakeUser() // we auth as normal user and can remove fake credentials
                    analytics.logEvent(Analytics.Login.SUCCESS_LOGIN_WITH_PASSWORD)
                }
                .andThen(expManager.reset()) // reset rating from previous account
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe(this::onSuccess, this::handleLoginError)
    }

    private fun handleLoginError(error: Throwable) {
        val authError = if (error is HttpException) {
            if (error.code() == 429) {
                AuthError.TooManyAttempts
            } else {
                AuthError.EmailPasswordInvalid
            }
        } else {
            AuthError.ConnectionProblem
        }
        onError(authError)
    }

    private fun createAccountRx(credentials: AccountCredentials): Completable =
            authRepository.createAccount(credentials.toRegistrationUser())

    private fun loginRx(login: String, password: String): Completable =
            authRepository.authWithLoginPassword(login, password).toCompletable()

    private fun createFakeUserRx(): Completable =
            Single.fromCallable { RxOptional(profilePreferences.fakeUser) }.flatMapCompletable { optional ->
                val credentials = optional.value ?: api.createFakeAccount()
                if (optional.value == null) {
                    profilePreferences.fakeUser = credentials
                    createAccountRx(credentials) then loginRx(credentials.login, credentials.password)
                } else {
                    loginRx(credentials.login, credentials.password).onErrorResumeNext { error ->
                        if (error is HttpException && error.code() == 401) { // on some reason we cannot authorize user with old fake account credential
                            profilePreferences.fakeUser = null // remove old fake user
                            createFakeUserRx() // retry
                        } else {
                            Completable.error(error)
                        }
                    }
                }
            }

    private fun onLoginRx(): Completable = api
            .joinCourse(questionsPacksManager.currentCourseId)
            .andThen(profileRepository.fetchProfileWithEmailAddresses())
            .doOnSuccess {
                profilePreferences.profile = it
                analytics.logEvent(Analytics.Login.SUCCESS_LOGIN)
            }
            .flatMapCompletable {
                it.subscribedForMail = false
                profileRepository.updateProfile(it)
            }

    private fun onError(authError: AuthError) {
        analytics.logEventWithName(Analytics.Login.FAIL_LOGIN, authError.name)
        view?.onError(authError)
    }

    private fun onSuccess() {
        isSuccess = true
        analytics.successLogin()
        view?.onSuccess()
    }

    override fun attachView(view: AuthView) {
        super.attachView(view)
        if (isSuccess) view.onSuccess()
    }

    override fun destroy() {
        disposable.dispose()
    }
}