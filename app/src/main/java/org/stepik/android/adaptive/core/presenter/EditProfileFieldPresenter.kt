package org.stepik.android.adaptive.core.presenter

import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileFieldView
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import javax.inject.Inject


class EditProfileFieldPresenter
@Inject
constructor(
        private val profieRepository: ProfileRepository,
        private val profilePreferences: ProfilePreferences
): PresenterBase<EditProfileFieldView>() {
    private val compositeDisposable = CompositeDisposable()



    override fun destroy() =
            compositeDisposable.dispose()
}