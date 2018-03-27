package org.stepik.android.adaptive.core.events

interface Client<T> {
    fun subscribe(listener: T)
    fun unsubscribe(listener: T)
}
