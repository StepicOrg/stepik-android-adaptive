package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class Course(
    val id: Long,
    @SerializedName("total_units")
    val totalUnits: Int,
    @SerializedName("enrollment")
    var enrollment: Long = 0,
    @SerializedName("price_tier")
    val priceTier: String? = null
)
