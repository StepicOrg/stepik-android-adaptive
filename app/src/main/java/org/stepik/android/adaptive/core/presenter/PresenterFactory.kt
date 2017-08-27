package org.stepik.android.adaptive.core.presenter


interface PresenterFactory<out P : Presenter<*>> {
    fun create() : P
}