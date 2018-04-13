package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileView
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.addDisposable
import javax.inject.Inject

class EditProfilePresenter
@Inject
constructor(
        private val profileRepository: ProfileRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,

        private val profilePreferences: ProfilePreferences
): PresenterBase<EditProfileView>() {
    private val compositeDisposable = CompositeDisposable()

    private var state: EditProfileView.State = EditProfileView.State.ProfileLoading
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        compositeDisposable addDisposable profileRepository.fetchProfile() // if profile is null we display an error
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    state = EditProfileView.State.ProfileLoaded(it)
                }, {
                    it.printStackTrace()
                    state = EditProfileView.State.ProfileLoadingError
                })
    }

    fun updateProfile(firstName: String, lastName: String) {
        compositeDisposable addDisposable profileRepository.updateProfile(Profile(firstName = firstName, lastName = lastName))
                .subscribe()
    }

    override fun attachView(view: EditProfileView) {
        super.attachView(view)
        view.setState(state)
    }

    override fun destroy() =
            compositeDisposable.dispose()
}