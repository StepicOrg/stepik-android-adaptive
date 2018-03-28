package org.stepik.android.adaptive.di.storage

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepik.android.adaptive.data.db.DataBaseMgr

@Component(modules = [StorageModule::class])
@StorageSingleton
interface StorageComponent {
    @Component.Builder
    interface Builder {
        fun build(): StorageComponent

        @BindsInstance
        fun context(context: Context): Builder
    }

    val dataBaseMgr: DataBaseMgr
}