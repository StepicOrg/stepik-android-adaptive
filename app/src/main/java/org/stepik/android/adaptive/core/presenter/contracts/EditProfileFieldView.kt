package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.api.profile.model.EditEmailError
import org.stepik.android.adaptive.api.profile.model.EditNameError
import org.stepik.android.adaptive.api.profile.model.EditPasswordError
import org.stepik.android.adaptive.data.model.Profile

interface EditProfileFieldView {
    fun setState(state: State)
    fun onProfile(profile: Profile)

    sealed class State {
        object Loading : State()
        object Success : State()
        object ProfileLoaded : State()
        class NameError(val error: EditNameError) : State()
        class EmailError(val error: EditEmailError) : State()
        class PasswordError(val error: EditPasswordError) : State()
        object NetworkError : State()
    }
}
