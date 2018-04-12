package org.stepik.android.adaptive.di.login

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.activity.LoginActivity
import org.stepik.android.adaptive.ui.activity.SocialAuthActivity
import org.stepik.android.adaptive.ui.dialog.RemindPasswordDialog

@Subcomponent
interface LoginComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LoginComponent
    }

    fun inject(activity: SocialAuthActivity)
    fun inject(activity: LoginActivity)

    fun inject(dialog: RemindPasswordDialog)
}