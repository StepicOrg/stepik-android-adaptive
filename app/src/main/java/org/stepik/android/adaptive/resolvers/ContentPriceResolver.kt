package org.stepik.android.adaptive.resolvers

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.configuration.RemoteConfig
import org.stepik.android.adaptive.data.model.InAppPurchasePrice
import javax.inject.Inject

class ContentPriceResolver
@Inject
constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    private val pricesMap by lazy {
        val mapTypeToken = object : TypeToken<Map<String, InAppPurchasePrice>>() {}.type
        try {
            Gson().fromJson<Map<String, InAppPurchasePrice>>(firebaseRemoteConfig.getString(RemoteConfig.IN_APP_PRICES_MAP), mapTypeToken)
        } catch (_: Exception) {
            emptyMap<String, InAppPurchasePrice>()
        }
    }

    fun resolveSkuPrice(sku: Sku): Double =
        pricesMap[sku.id.code]?.let { price -> price.additionalPrices?.get(sku.detailedPrice.currency) ?: price.price } ?: 0.0
}