package org.stepik.android.adaptive

import android.app.Application
import com.vk.sdk.VKSdk
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.branch.referral.Branch
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.analytics.experiments.SplitTestsHolder
import org.stepik.android.adaptive.di.AppCoreComponent
import org.stepik.android.adaptive.di.ComponentManager
import org.stepik.android.adaptive.di.DaggerAppCoreComponent
import org.stepik.android.adaptive.di.storage.DaggerStorageComponent
import org.stepik.android.adaptive.notifications.NotificationChannelInitializer
import org.stepik.android.adaptive.util.DebugToolsHelper
import org.stepik.android.adaptive.util.isMainProcess
import javax.inject.Inject

class App : Application() {
    companion object {
        lateinit var app: App
            private set

        fun component(): AppCoreComponent =
            app.component
        fun componentManager(): ComponentManager =
            app.componentManager
    }

    private lateinit var component: AppCoreComponent
    private lateinit var componentManager: ComponentManager

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var splitTestsHolder: SplitTestsHolder

    override fun onCreate() {
        super.onCreate()
        if (!isMainProcess) return

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
    }

    private fun initServices() {
        VKSdk.initialize(applicationContext)

        YandexMetrica.activate(applicationContext, YandexMetricaConfig.newConfigBuilder(config.appMetricaKey).build())
        YandexMetrica.enableActivityAutoTracking(this)

        DebugToolsHelper.initDebugTools(this)
        Branch.getAutoInstance(this)
    }
}
