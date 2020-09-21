package org.stepik.android.adaptive.util

import java.util.Random

object KotlinUtils {
    inline fun <T> setIfNot(setter: (T) -> Unit, value: T, not: T) {
        if (value != not) {
            setter(value)
        }
    }
}

fun IntRange.random(): Int =
    Random().nextInt((endInclusive + 1) - start) +  start
