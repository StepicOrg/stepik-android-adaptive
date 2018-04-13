package org.stepik.android.adaptive.core.presenter

import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.api.profile.model.ProfileCompositeError
import org.stepik.android.adaptive.core.presenter.contracts.RegisterView
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.addDisposable
import org.stepik.android.adaptive.util.then
import retrofit2.HttpException
import javax.inject.Inject

@AppSingleton
class RegisterPresenter
@Inject
constructor(
        private val profileRepository: ProfileRepository,
        private val profilePreferences: ProfilePreferences,
        private val sharedPreferenceHelper: SharedPreferenceHelper,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
        ): PresenterBase<RegisterView>() {
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    private var state: RegisterView.State = RegisterView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    fun register(firstName: String, lastName: String, email: String, password: String) {
        state = RegisterView.State.Loading

        compositeDisposable addDisposable profileRepository.fetchProfile().flatMap { profile ->
            profile.firstName = firstName
            profile.lastName = lastName

            profileRepository.updateProfile(profile).doOnComplete { profilePreferences.profile = profile } then profileRepository.updateEmail(email) then Single.just(profile.id)
        }.flatMapCompletable { profileId ->
            val oldPassword = sharedPreferenceHelper.fakeUser?.password ?: ""
            profileRepository.updatePassword(profileId, oldPassword, password)
        }.subscribeOn(backgroundScheduler).observeOn(mainScheduler).doOnComplete {
            sharedPreferenceHelper.removeFakeUser()
        }.subscribe({
            state = RegisterView.State.Success
        }, {
            it.printStackTrace()
            val errorMessage = if (it is HttpException) {
                val error = gson.fromJson(it.response()?.errorBody()?.string() ?: "", ProfileCompositeError::class.java)
                error.asList.filterNotNull().firstOrNull() ?: ""
            } else {
                ""
            }
            state = RegisterView.State.Error(errorMessage)
        })
    }

    override fun attachView(view: RegisterView) {
        super.attachView(view)
        view.setState(state)
    }

    override fun destroy() =
            compositeDisposable.dispose()
}