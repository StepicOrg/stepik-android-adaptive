package org.stepik.android.adaptive

import android.app.Application
import com.yandex.metrica.YandexMetrica
import org.solovyev.android.checkout.Billing
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppCoreComponent
import org.stepik.android.adaptive.di.ComponentManager
import org.stepik.android.adaptive.di.DaggerAppCoreComponent

class App : Application() {
    companion object {
        private lateinit var app: App

        fun component() = app.component
        fun componentManager() = app.componentManager
    }

    private lateinit var component: AppCoreComponent
    private lateinit var componentManager: ComponentManager

    val billing by lazy {
        Billing(this, object : Billing.DefaultConfiguration() {
            override fun getPublicKey() = Config.getInstance().appPublicLicenseKey
        })
    }

    override fun onCreate() {
        super.onCreate()
        app = this

        component = DaggerAppCoreComponent
                .builder()
                .context(applicationContext)
                .build()
        componentManager = ComponentManager(component)

        Util.initMgr(applicationContext)

        YandexMetrica.activate(applicationContext, Config.getInstance().appMetricaKey)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
