package org.stepik.android.adaptive.core.presenter

import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.api.profile.model.EditNameError
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileFieldView
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
                        EditProfileFieldView.State.ProfileLoaded(it)
                    } else {
                        EditProfileFieldView.State.NetworkError
                    }
                }, {
                    viewState = EditProfileFieldView.State.NetworkError
                })
    }

    fun syncName(firstName: String, lastName: String) = viewState.let { state ->
        if (state is EditProfileFieldView.State.ProfileLoaded) {
            state.profile.firstName = firstName
            state.profile.lastName = lastName
        }
    }

    fun changeName(firstName: String, lastName: String) = viewState.let { state ->
        viewState = EditProfileFieldView.State.Loading
        if (state is EditProfileFieldView.State.ProfileLoaded) {
            val profile = state.profile.copy(firstName = firstName, lastName = lastName)

            compositeDisposable addDisposable profileRepository.updateProfile(profile)
                    .andThen(profileRepository.fetchProfileWithEmailAddresses())
                    .doOnSuccess { profilePreferences.profile = it }
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribe({
                        viewState = EditProfileFieldView.State.Success
                    }, {
                        viewState = if (it is HttpException) {
                            val error = gson.fromJson(it.response()?.errorBody()?.string(), EditNameError::class.java)
                            if (error != null) {
                                EditProfileFieldView.State.NameError(error)
                            } else {
                                EditProfileFieldView.State.NetworkError
                            }
                        } else {
                            EditProfileFieldView.State.NetworkError
                        }
                    })
        }
    }

    override fun attachView(view: EditProfileFieldView) {
        super.attachView(view)
        view.setState(viewState)
    }

    override fun destroy() =
            compositeDisposable.dispose()
}