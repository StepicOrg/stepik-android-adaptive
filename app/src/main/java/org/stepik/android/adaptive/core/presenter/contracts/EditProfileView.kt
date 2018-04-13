package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.data.model.Profile

interface EditProfileView {
    fun setState(state: State)

    sealed class State {
        object ProfileLoading: State()
        class ProfileLoaded(val profile: Profile): State()
        object ProfileLoadingError: State()
    }
}