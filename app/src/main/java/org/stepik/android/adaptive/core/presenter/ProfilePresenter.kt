package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.core.presenter.contracts.ProfileView
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.addDisposable
import javax.inject.Inject

class ProfilePresenter
@Inject
constructor(
    private val profilePreferences: ProfilePreferences,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileView>() {
    private val compositeDisposable = CompositeDisposable()

    private var viewState: ProfileView.State = ProfileView.State.Idle
        set(value) {
            field = value
            view?.setState(viewState)
        }

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        compositeDisposable addDisposable profilePreferences.isFakeUser()
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribe({ isFake: Boolean ->
                if (isFake) {
                    viewState = ProfileView.State.EmptyAuth
                } else {
                    fetchLocalProfile()
                }
            })
    }

    private fun fetchLocalProfile() {
        compositeDisposable addDisposable Single.fromCallable(profilePreferences::profile)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribe(
                {
                    viewState = if (it != null) {
                        ProfileView.State.ProfileLoaded(it)
                    } else {
                        ProfileView.State.Error
                    }
                },
                {
                    viewState = ProfileView.State.Error
                }
            )
    }

    override fun attachView(view: ProfileView) {
        super.attachView(view)

        view.setState(viewState)
    }

    override fun destroy() {
        compositeDisposable.dispose()
    }
}
