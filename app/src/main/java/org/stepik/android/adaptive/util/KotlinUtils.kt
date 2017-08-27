package org.stepik.android.adaptive.util

object KotlinUtils {
    inline fun <T> setIfNot(setter: (T) -> Unit, value: T, not: T) {
        if (value != not) {
            setter(value)
        }
    }
}