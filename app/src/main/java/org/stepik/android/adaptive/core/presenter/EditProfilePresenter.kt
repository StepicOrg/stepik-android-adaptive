package org.stepik.android.adaptive.core.presenter

import org.stepik.android.adaptive.core.presenter.contracts.EditProfileView
import javax.inject.Inject

class EditProfilePresenter
@Inject
constructor(): PresenterBase<EditProfileView>() {
    override fun destroy() {}
}