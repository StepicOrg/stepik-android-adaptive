package org.stepik.android.adaptive.api.profile.model

class ProfileCompositeError(
        val email: String?
) {
    val asList: List<String?>
        get() = listOf(email)
}