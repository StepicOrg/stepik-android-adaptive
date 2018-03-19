package org.stepik.android.adaptive

import android.support.multidex.MultiDexApplication
import com.yandex.metrica.YandexMetrica
import org.solovyev.android.checkout.Billing
import org.stepik.android.adaptive.configuration.Config

class App : MultiDexApplication() {
    val billing by lazy {
        Billing(this, object : Billing.DefaultConfiguration() {
            override fun getPublicKey() = Config.getInstance().appPublicLicenseKey
        })
    }

    override fun onCreate() {
        super.onCreate()
        Util.initMgr(applicationContext)

        YandexMetrica.activate(applicationContext, Config.getInstance().appMetricaKey)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
