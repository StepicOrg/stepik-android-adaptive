package org.stepik.android.adaptive

import android.app.Application
import com.facebook.FacebookSdk
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppCoreComponent
import org.stepik.android.adaptive.di.ComponentManager
import org.stepik.android.adaptive.di.DaggerAppCoreComponent
import org.stepik.android.adaptive.di.storage.DaggerStorageComponent
import org.stepik.android.adaptive.notifications.NotificationChannelInitializer
import org.stepik.android.adaptive.util.StethoHelper
import javax.inject.Inject

class App : Application() {
    companion object {
        lateinit var app: App
            private set

        fun component() = app.component
        fun componentManager() = app.componentManager
    }

    private lateinit var component: AppCoreComponent
    private lateinit var componentManager: ComponentManager

    @Inject
    lateinit var config: Config

    override fun onCreate() {
        super.onCreate()
        app = this

        component = DaggerAppCoreComponent
                .builder()
                .setStorageComponent(
                        DaggerStorageComponent.builder()
                                .context(applicationContext)
                                .build()
                )
                .context(applicationContext)
                .build()
        componentManager = ComponentManager(component)
        component.inject(this)

        initServices()
        NotificationChannelInitializer.initNotificationChannel(this)

        StethoHelper.initStetho(this)
    }

    private fun initServices() {
        VKSdk.initialize(applicationContext)
        FacebookSdk.sdkInitialize(applicationContext)

        YandexMetrica.activate(applicationContext, YandexMetricaConfig.newConfigBuilder(config.appMetricaKey).build())
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
