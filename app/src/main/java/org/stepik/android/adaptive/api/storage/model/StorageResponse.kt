package org.stepik.android.adaptive.api.storage.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.adaptive.data.model.Meta
import org.stepik.android.adaptive.data.model.StorageRecord

class StorageResponse(
        val meta: Meta?,
        @SerializedName("storage-records") val records: List<StorageRecord>
)