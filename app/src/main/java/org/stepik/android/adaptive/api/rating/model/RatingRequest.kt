package org.stepik.android.adaptive.api.rating.model

data class RatingRequest(
        @JvmField val exp: Long,
        @JvmField val course: Long,
        @JvmField val token: String?)