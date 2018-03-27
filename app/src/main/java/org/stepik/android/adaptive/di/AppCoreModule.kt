package org.stepik.android.adaptive.di

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.solovyev.android.checkout.Billing
import org.stepik.android.adaptive.BuildConfig
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.core.events.Client
import org.stepik.android.adaptive.core.events.ClientImpl
import org.stepik.android.adaptive.core.events.ListenerContainer
import org.stepik.android.adaptive.core.events.ListenerContainerImpl
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.achievements.AchievementEventListener

@Module
abstract class AppCoreModule {

    @Binds
    @AppSingleton
    abstract fun provideAchievementEventClient(container: ClientImpl<AchievementEventListener>): Client<AchievementEventListener>

    @Binds
    @AppSingleton
    abstract fun provideAchievementEventListenerContainer(container: ListenerContainerImpl<AchievementEventListener>): ListenerContainer<AchievementEventListener>

    @Module
    companion object {
        @Provides
        @JvmStatic
        @MainScheduler
        internal fun provideAndroidScheduler(): Scheduler = AndroidSchedulers.mainThread()

        @Provides
        @JvmStatic
        @BackgroundScheduler
        internal fun provideBackgroundScheduler(): Scheduler = Schedulers.io()

        @JvmStatic
        @Provides
        @AppSingleton
        internal fun provideBilling(context: Context, config: Config.Configuration): Billing =
                Billing(context, object : Billing.DefaultConfiguration() {
                    override fun getPublicKey() = config.appPublicLicenseKey
                })

        @JvmStatic
        @Provides
        @AppSingleton
        internal fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig =
                FirebaseRemoteConfig.getInstance().apply {
                    val configSettings = FirebaseRemoteConfigSettings.Builder()
                            .setDeveloperModeEnabled(BuildConfig.DEBUG)
                            .build()
                    setConfigSettings(configSettings)
                    setDefaults(R.xml.remote_config_defaults)
                }
    }
}