package org.stepik.android.adaptive.pdd.api

data class RatingRequest(
        @JvmField val exp: Long,
        @JvmField val course: Long,
        @JvmField val token: String?)