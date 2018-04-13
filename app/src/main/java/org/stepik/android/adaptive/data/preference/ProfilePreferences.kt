package org.stepik.android.adaptive.data.preference

import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile

interface ProfilePreferences {
    val profileId: Long
    var profile: Profile?

    var fakeUser: AccountCredentials?
    fun isFakeUser(): Boolean
    fun removeFakeUser()
}