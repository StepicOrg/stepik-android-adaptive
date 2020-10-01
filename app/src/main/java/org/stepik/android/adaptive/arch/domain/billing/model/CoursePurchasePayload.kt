package org.stepik.android.adaptive.arch.domain.billing.model

import com.google.gson.annotations.SerializedName

data class CoursePurchasePayload(
    @SerializedName("profile_id")
    val profileId: Long,
    @SerializedName("course_id")
    val courseId: Long
)
