package org.stepik.android.adaptive.data.model

import com.google.gson.annotations.SerializedName

class InAppPurchasePrice(
    @SerializedName("price")
    val price: Double,

    @SerializedName("additional_prices")
    val additionalPrices: Map<String, Double>?
)
