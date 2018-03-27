package org.stepik.android.adaptive.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.core.presenter.CardPresenter
import org.stepik.android.adaptive.di.login.LoginComponent
import org.stepik.android.adaptive.di.paid_content.PaidContentComponent
import org.stepik.android.adaptive.di.stats.StatsComponent
import org.stepik.android.adaptive.di.study.StudyComponent
import org.stepik.android.adaptive.receivers.BootCompletedReceiver
import org.stepik.android.adaptive.receivers.NotificationsReceiver
import org.stepik.android.adaptive.ui.activity.SplashActivity
import org.stepik.android.adaptive.ui.dialog.*
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment

@AppSingleton
@Component(modules = [AppCoreModule::class])
interface AppCoreComponent {

    @Component.Builder
    interface Builder {
        fun build(): AppCoreComponent

        @BindsInstance
        fun context(context: Context): Builder
    }

    fun statsComponentBuilder(): StatsComponent.Builder

    fun studyComponentBuilder(): StudyComponent.Builder

    fun paidContentComponentBuilder(): PaidContentComponent.Builder

    fun loginComponentBuilder(): LoginComponent.Builder

    fun inject(app: App)

    fun inject(dialog: QuestionsPacksDialog)
    fun inject(dialog: StreakRestoreDialog)
    fun inject(dialog: InventoryDialog)
    fun inject(dialog: RateAppDialog)
    fun inject(dialog: LogoutDialog)

    fun inject(activity: SplashActivity)

    fun inject(notificationsReceiver: NotificationsReceiver)

    fun inject(bootCompletedReceiver: BootCompletedReceiver)
}