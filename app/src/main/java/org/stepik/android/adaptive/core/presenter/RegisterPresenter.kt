package org.stepik.android.adaptive.core.presenter

import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.api.profile.model.ProfileCompositeError
import org.stepik.android.adaptive.core.presenter.contracts.RegisterView
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.ValidateUtil
import org.stepik.android.adaptive.util.then
import retrofit2.HttpException
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

@AppSingleton
class RegisterPresenter
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val profilePreferences: ProfilePreferences,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val analytics: Analytics
) : PresenterBase<RegisterView>() {
    private val gson = Gson()

    private var state: RegisterView.State = RegisterView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private fun validate(firstName: String, lastName: String, email: String, password: String): Boolean {
        if (!ValidateUtil.isEmailValid(email)) {
            state = RegisterView.State.EmptyEmailError
            return false
        }

        return true
    }

    fun register(firstName: String, lastName: String, email: String, password: String) {
        if (!validate(firstName, lastName, email, password)) return

        state = RegisterView.State.Loading

        compositeDisposable += profileRepository.fetchProfile().flatMap { profile ->
            profile.firstName = firstName
            profile.lastName = lastName

            profileRepository.updateProfile(profile) then
                profileRepository.updateEmail(email) then
                profileRepository.fetchProfileWithEmailAddresses().doOnSuccess { profilePreferences.profile = it }.map { it.id }
        }.flatMapCompletable { profileId ->
            val oldPassword = profilePreferences.fakeUser?.password ?: ""
            profileRepository.updatePassword(profileId, oldPassword, password)
        }.subscribeOn(backgroundScheduler).observeOn(mainScheduler).doOnComplete {
            profilePreferences.removeFakeUser()
        }.subscribe(
            {
                state = RegisterView.State.Success
                analytics.logEvent(Analytics.Registration.SUCCESS_REGISTER)
            },
            {
                state = if (it is HttpException) {
                    val error = gson.fromJson(it.response()?.errorBody()?.string(), ProfileCompositeError::class.java)
                    val errorMessage = error?.asList?.filterNotNull()?.firstOrNull()
                    if (errorMessage != null) {
                        RegisterView.State.Error(errorMessage)
                    } else {
                        RegisterView.State.NetworkError
                    }
                } else {
                    RegisterView.State.NetworkError
                }
                analytics.logEventWithName(Analytics.Registration.ERROR, it?.message ?: "")
            }
        )
    }

    override fun attachView(view: RegisterView) {
        super.attachView(view)
        view.setState(state)
    }
}
