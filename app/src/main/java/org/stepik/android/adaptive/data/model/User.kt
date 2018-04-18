package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class User(
        val id: Long = 0,
        @SerializedName("full_name")
        val fullName: String,
        val city: Long?
)