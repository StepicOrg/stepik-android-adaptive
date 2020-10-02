package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class Course(
    val id: Long,
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("total_units")
    val totalUnits: Int,
    @SerializedName("enrollment")
    val enrollment: Long = 0,
    @SerializedName("price_tier")
    val priceTier: String? = null,
    @SerializedName("is_paid")
    val isPaid: Boolean = false
)
