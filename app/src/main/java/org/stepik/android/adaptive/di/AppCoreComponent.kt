package org.stepik.android.adaptive.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.di.content.questions.QuestionsModule
import org.stepik.android.adaptive.di.login.LoginComponent
import org.stepik.android.adaptive.di.network.NetworkModule
import org.stepik.android.adaptive.di.paid_content.PaidContentComponent
import org.stepik.android.adaptive.di.stats.StatsComponent
import org.stepik.android.adaptive.di.storage.StorageComponent
import org.stepik.android.adaptive.di.study.StudyComponent
import org.stepik.android.adaptive.receivers.BootCompletedReceiver
import org.stepik.android.adaptive.receivers.NotificationsReceiver
import org.stepik.android.adaptive.ui.activity.SplashActivity
import org.stepik.android.adaptive.ui.adapter.QuizCardViewHolder
import org.stepik.android.adaptive.ui.dialog.*

@AppSingleton
@Component(modules = [AppCoreModule::class, NetworkModule::class, QuestionsModule::class], dependencies = [StorageComponent::class])
interface AppCoreComponent {

    @Component.Builder
    interface Builder {
        fun build(): AppCoreComponent

        fun setStorageComponent(storageComponent: StorageComponent): Builder

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

    fun inject(viewHolder: QuizCardViewHolder)

    fun inject(activity: SplashActivity)

    fun inject(notificationsReceiver: NotificationsReceiver)

    fun inject(bootCompletedReceiver: BootCompletedReceiver)
}
