package org.stepik.android.adaptive.core.events

interface ListenerContainer<T>{
    fun add(listener: T)
    fun remove(listener: T)
    fun asIterable(): Iterable<T>
}
