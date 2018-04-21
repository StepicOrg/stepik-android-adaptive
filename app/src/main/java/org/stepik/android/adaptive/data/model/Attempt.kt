package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class Attempt (
        val id: Long = 0,
        val step: Long = 0,
        @SerializedName("dataset")
        val datasetWrapper: DatasetWrapper? = null,
        val dataset_url: String? = null,
        val time: String? = null,
        val status: String? = null,
        val time_left: String? = null,
        val user: Long = 0
) {
    fun getDataset() = datasetWrapper?.dataset
}
