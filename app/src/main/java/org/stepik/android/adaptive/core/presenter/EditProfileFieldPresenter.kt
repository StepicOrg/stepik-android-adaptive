package org.stepik.android.adaptive.core.presenter

import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileFieldView
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.addDisposable
import retrofit2.HttpException
import javax.inject.Inject

class EditProfileFieldPresenter
@Inject
constructor(
        private val profileRepository: ProfileRepository,
        private val profilePreferences: ProfilePreferences,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<EditProfileFieldView>() {
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()

    private var viewState: EditProfileFieldView.State = EditProfileFieldView.State.Loading
        set(value) {
            field = value
            view?.setState(viewState)
        }

    init {
        compositeDisposable addDisposable Single.fromCallable(profilePreferences::profile)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    viewState = if (it != null) {
                        view?.onProfile(it)
                        EditProfileFieldView.State.ProfileLoaded
                    } else {
                        EditProfileFieldView.State.NetworkError
                    }
                }, {
                    viewState = EditProfileFieldView.State.NetworkError
                })
    }

    private fun syncProfile(): Single<Profile> =
            profileRepository
                    .fetchProfileWithEmailAddresses()
                    .doOnSuccess { profilePreferences.profile = it }

    fun changeName(firstName: String, lastName: String) {
        viewState = EditProfileFieldView.State.Loading

        compositeDisposable addDisposable Single.fromCallable(profilePreferences::profile)
                .flatMapCompletable {
                    it.firstName = firstName
                    it.lastName = lastName
                    profileRepository.updateProfile(it)
                }
                .andThen(syncProfile())
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    viewState = EditProfileFieldView.State.Success
                }) {
                    viewState = parseErrorState(it, EditProfileFieldView.State::NameError)
                }
    }

    fun changeEmail(email: String) {
        viewState = EditProfileFieldView.State.Loading

        compositeDisposable addDisposable profileRepository.updateEmail(email)
                .andThen(syncProfile())
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    viewState = EditProfileFieldView.State.Success
                }) {
                    viewState = parseErrorState(it, EditProfileFieldView.State::EmailError)
                }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewState = EditProfileFieldView.State.Loading

        compositeDisposable addDisposable Single.fromCallable(profilePreferences::profileId)
                .flatMapCompletable {
                    profileRepository.updatePassword(it, oldPassword, newPassword)
                }
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe({
                    viewState = EditProfileFieldView.State.Success
                }) {
                    viewState = parseErrorState(it, EditProfileFieldView.State::PasswordError)
                }
    }

    private inline fun <reified T> parseErrorState(throwable: Throwable, stateConstructor: (T) -> EditProfileFieldView.State) =
            if (throwable is HttpException) {
                val error = gson.fromJson(throwable.response()?.errorBody()?.string(), T::class.java)
                if (error != null) {
                    stateConstructor(error)
                } else {
                    EditProfileFieldView.State.NetworkError
                }
            } else {
                EditProfileFieldView.State.NetworkError
            }

    override fun attachView(view: EditProfileFieldView) {
        super.attachView(view)
        view.setState(viewState)
    }

    override fun destroy() =
            compositeDisposable.dispose()
}