package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.api.profile.model.EditNameError
import org.stepik.android.adaptive.data.model.Profile

interface EditProfileFieldView {
    fun setState(state: State)

    sealed class State {
        object Loading: State()
        object Success: State()
        class ProfileLoaded(val profile: Profile): State()
        class NameError(val error: EditNameError): State()
        object NetworkError: State()
    }
}