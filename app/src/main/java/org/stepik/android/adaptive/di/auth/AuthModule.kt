package org.stepik.android.adaptive.di.auth

import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.qualifiers.AuthLock
import java.util.concurrent.locks.ReentrantLock

@Module
abstract class AuthModule {

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        @AuthLock
        internal fun provideAuthLock(): ReentrantLock = ReentrantLock()
    }

}