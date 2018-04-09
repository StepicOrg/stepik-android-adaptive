package org.stepik.android.adaptive.api.storage.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.adaptive.data.model.StorageRecord

class StorageRequest(
        @SerializedName("storage-record") val record: StorageRecord
)