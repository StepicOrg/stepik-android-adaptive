package org.stepik.android.adaptive.core.presenter

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel

abstract class PresenterBase<V> : Presenter<V>, ViewModel() {
    @Volatile
    var view: V? = null
        private set

    @CallSuper
    override fun attachView(view: V) {
        val previousView = this.view

        if (previousView != null) {
            throw IllegalStateException("Previous view is not detached! previousView = " + previousView)
        }

        this.view = view
    }

    @CallSuper
    override fun detachView(view: V) {
        val previousView = this.view

        if (previousView === view) {
            this.view = null
        } else {
            throw IllegalStateException("Unexpected view! previousView = $previousView, getView to unbind = $view")
        }
    }
}