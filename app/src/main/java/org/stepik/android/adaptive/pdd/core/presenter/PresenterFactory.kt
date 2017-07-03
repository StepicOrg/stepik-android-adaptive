package org.stepik.android.adaptive.pdd.core.presenter


interface PresenterFactory<out P : Presenter<*>> {
    fun create() : P
}