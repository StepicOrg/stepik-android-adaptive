package org.stepik.android.adaptive.api

import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.model.User

class ProfileResponse(@JvmField val profiles: List<Profile>?) {
    val profile: Profile?
        get() = profiles?.firstOrNull()
}
