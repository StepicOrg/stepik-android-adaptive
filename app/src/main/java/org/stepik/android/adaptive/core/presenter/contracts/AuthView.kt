package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.api.auth.AuthError

interface AuthView {
    fun onSuccess()
    fun onError(authError: AuthError)
    fun onLoading()
}
