package org.stepik.android.adaptive.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepik.android.adaptive.ui.dialog.QuestionsPacksDialog
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

    fun inject(fragment: RecommendationsFragment)

    fun inject(dialog: QuestionsPacksDialog)
}