package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class StorageRecord<T>(
        val id: Long = -1,
        val user: Long = -1,
        val kind: String,
        val data: T,
        @SerializedName("create_date") val createDate: String? = null,
        @SerializedName("update_date") val updateDate: String? = null
)