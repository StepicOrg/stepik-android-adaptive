package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.data.model.Profile

interface ProfileView {
    fun setState(state: State)

    sealed class State {
        object Idle : State()
        object EmptyAuth : State()
        object Error : State()
        class ProfileLoaded(val profile: Profile) : State()
    }
}
