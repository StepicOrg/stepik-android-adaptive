package org.stepik.android.adaptive.api.profile.model

import org.stepik.android.adaptive.data.model.Profile

class ProfileResponse(@JvmField val profiles: List<Profile>?) {
    val profile: Profile?
        get() = profiles?.firstOrNull()
}
