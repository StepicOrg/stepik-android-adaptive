package org.stepik.android.adaptive.di.storage

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.data.db.DataBaseHelper
import org.stepik.android.adaptive.data.db.dao.BookmarksDao
import org.stepik.android.adaptive.data.db.dao.IDao
import org.stepik.android.adaptive.data.db.operations.DatabaseOperations
import org.stepik.android.adaptive.data.db.operations.DatabaseOperationsImpl
import org.stepik.android.adaptive.data.model.Bookmark

@Module
abstract class StorageModule {
    @StorageSingleton
    @Binds
    internal abstract fun provideSqlOpenHelper(databaseHelper: DataBaseHelper): SQLiteOpenHelper

    @StorageSingleton
    @Binds
    internal abstract fun provideBookmarksDao(bookmarksDao: BookmarksDao): IDao<Bookmark>

    @StorageSingleton
    @Binds
    internal abstract fun bindsOperations(databaseOperationsImpl: DatabaseOperationsImpl): DatabaseOperations

    @Module
    companion object {
        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideWritableDatabase(helper: SQLiteOpenHelper): SQLiteDatabase =
                helper.writableDatabase
    }

}