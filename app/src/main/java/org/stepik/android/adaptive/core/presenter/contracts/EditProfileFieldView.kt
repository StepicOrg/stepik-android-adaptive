package org.stepik.android.adaptive.core.presenter.contracts

interface EditProfileFieldView {
    fun setState(state: State)

    sealed class State {
        object Loading: State()
        object Success: State()
        object Error: State()
    }
}