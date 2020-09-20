package org.stepik.android.adaptive.core

interface LogoutHelper {
    fun logout(onComplete: (() -> Unit)?)
}
