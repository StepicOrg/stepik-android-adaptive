package org.stepik.android.adaptive

import android.app.Application
import com.yandex.metrica.YandexMetrica
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.di.AppCoreComponent
import org.stepik.android.adaptive.di.ComponentManager
import org.stepik.android.adaptive.di.DaggerAppCoreComponent
import javax.inject.Inject

class App : Application() {
    companion object {
        private lateinit var app: App

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
                .context(applicationContext)
                .build()
        componentManager = ComponentManager(component)
        component.inject(this)

        Util.initMgr(applicationContext)

        YandexMetrica.activate(applicationContext, config.appMetricaKey)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
