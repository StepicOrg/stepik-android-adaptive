package org.stepik.android.adaptive.di

import android.content.Context
import android.content.pm.PackageManager
import android.webkit.CookieManager
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.BuildConfig
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.arch.view.injection.billing.BillingDataModule
import org.stepik.android.adaptive.arch.view.injection.billing.BillingModule
import org.stepik.android.adaptive.arch.view.injection.billing.PublicLicenseKey
import org.stepik.android.adaptive.arch.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.configuration.ConfigImpl
import org.stepik.android.adaptive.core.LogoutHelper
import org.stepik.android.adaptive.core.LogoutHelperImpl
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.ScreenManagerImpl
import org.stepik.android.adaptive.core.events.Client
import org.stepik.android.adaptive.core.events.ClientImpl
import org.stepik.android.adaptive.core.events.ListenerContainer
import org.stepik.android.adaptive.core.events.ListenerContainerImpl
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.analytics.AnalyticsModule
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.achievements.AchievementEventListener
import org.stepik.android.adaptive.util.AppConstants
import ru.nobird.android.view.injection.base.presentation.DaggerViewModelFactory
import javax.inject.Named

@Module(
    includes = [
        AnalyticsModule::class,
        PresenterModule::class,
        BillingModule::class,
        BillingDataModule::class,
        CoursePaymentsDataModule::class
    ]
)
abstract class AppCoreModule {

    @Binds
    @AppSingleton
    abstract fun provideAchievementEventClient(container: ClientImpl<AchievementEventListener>): Client<AchievementEventListener>

    @Binds
    @AppSingleton
    abstract fun provideAchievementEventListenerContainer(container: ListenerContainerImpl<AchievementEventListener>): ListenerContainer<AchievementEventListener>

    @Binds
    @AppSingleton
    abstract fun provideAuthRepository(sharedPreferenceHelper: SharedPreferenceHelper): ProfilePreferences

    @Binds
    @AppSingleton
    abstract fun provideLogoutHelper(logoutHelperImpl: LogoutHelperImpl): LogoutHelper

    @Binds
    @AppSingleton
    abstract fun provideScreenManager(screenManagerImpl: ScreenManagerImpl): ScreenManager

    @Binds
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Module
    companion object {
        @Provides
        @JvmStatic
        @MainScheduler
        internal fun provideAndroidScheduler(): Scheduler =
            AndroidSchedulers.mainThread()

        @Provides
        @JvmStatic
        @BackgroundScheduler
        internal fun provideBackgroundScheduler(): Scheduler =
            Schedulers.io()

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideConfig(configFactory: ConfigImpl.ConfigFactory): Config =
            configFactory.create()

        @Provides
        @AppSingleton
        @JvmStatic
        @PublicLicenseKey
        internal fun providePublicLicenseKey(config: Config): String =
            config.appPublicLicenseKey

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

        @JvmStatic
        @Provides
        @AppSingleton
        @Named(AppConstants.userAgentName)
        internal fun provideUserAgent(context: Context): String =
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val apiLevel = android.os.Build.VERSION.SDK_INT
                (
                    "StepikDroid/" + packageInfo.versionName + " (Android " + apiLevel +
                        ") build/" + packageInfo.versionCode + " package/" + packageInfo.packageName
                    )
            } catch (e: PackageManager.NameNotFoundException) {
                ""
            }

        @JvmStatic
        @Provides
        @AppSingleton
        internal fun provideCookieManager(): CookieManager =
            CookieManager.getInstance()
    }
}
