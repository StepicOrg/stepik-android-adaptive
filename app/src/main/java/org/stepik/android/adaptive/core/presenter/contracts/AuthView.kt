package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.api.auth.AuthError

interface LoginView {
    fun onSuccess()
    fun onError(authError: AuthError)
    fun onLoading()
}