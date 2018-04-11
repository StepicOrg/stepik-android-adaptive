package org.stepik.android.adaptive.core.presenter

import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.core.presenter.contracts.ProfileView
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import javax.inject.Inject


class ProfilePresenter
@Inject
constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper
): PresenterBase<ProfileView>() {
    private val compositeDisposable = CompositeDisposable()

    private var viewState: ProfileView.State = ProfileView.State.Idle
        set(value) {
            field = value
            view?.setState(viewState)
        }

    init {
        viewState = if (sharedPreferenceHelper.isFakeUser()) {
            ProfileView.State.EmptyAuth
        } else {
            ProfileView.State.Idle
        }
    }

    override fun attachView(view: ProfileView) {
        super.attachView(view)

        view.setState(viewState)
    }

    override fun destroy() = compositeDisposable.dispose()
}