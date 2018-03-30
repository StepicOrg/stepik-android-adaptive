package org.stepik.android.adaptive.data

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class SharedPreferenceDelegate<T>(
        private val key: String,
        private val getter: SharedPreferenceHelper.(String) -> T,
        private val setter: SharedPreferenceHelper.(String, T) -> Unit
): ReadWriteProperty<SharedPreferenceHelper, T>, ReadOnlyProperty<SharedPreferenceHelper, T> {
    override fun getValue(thisRef: SharedPreferenceHelper, property: KProperty<*>) = thisRef.getter(key)
    override fun setValue(thisRef: SharedPreferenceHelper, property: KProperty<*>, value: T) = thisRef.setter(key, value)

    class LongDelegate(key: String):    SharedPreferenceDelegate<Long>(key, SharedPreferenceHelper::getLong, SharedPreferenceHelper::saveLong)
    class IntDelegate(key: String):     SharedPreferenceDelegate<Int>(key, SharedPreferenceHelper::getInt, SharedPreferenceHelper::saveInt)
    class BooleanDelegate(key: String): SharedPreferenceDelegate<Boolean>(key, SharedPreferenceHelper::getBoolean, SharedPreferenceHelper::saveBoolean)
}

fun sharedInt(key: String)     = SharedPreferenceDelegate.IntDelegate(key)
fun sharedLong(key: String)    = SharedPreferenceDelegate.LongDelegate(key)
fun sharedBoolean(key: String) = SharedPreferenceDelegate.BooleanDelegate(key)