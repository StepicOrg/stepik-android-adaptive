package org.stepik.android.adaptive.core.presenter.contracts

interface ProfileView {
    fun setState(state: State)

    sealed class State {
        object Idle: State()
        object EmptyAuth: State()
    }
}