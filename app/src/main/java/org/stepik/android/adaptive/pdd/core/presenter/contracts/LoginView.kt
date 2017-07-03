package org.stepik.android.adaptive.pdd.core.presenter.contracts

interface LoginView {
    fun onSuccess()
    fun onNetworkError()
    fun onError(errorBody: String)
    fun onLoading()
}