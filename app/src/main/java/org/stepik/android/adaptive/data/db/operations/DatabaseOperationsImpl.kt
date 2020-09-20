package org.stepik.android.adaptive.data.db.operations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.di.qualifiers.DatabaseLock
import org.stepik.android.adaptive.di.storage.StorageSingleton
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

@StorageSingleton
class DatabaseOperationsImpl
@Inject
constructor(
    private val database: SQLiteDatabase,
    @DatabaseLock
    private val databaseLock: ReentrantReadWriteLock
) : DatabaseOperations {
    override fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: (Cursor) -> U): Single<U> =
        Single.create { emitter ->
            databaseLock.read {
                emitter.onSuccess(database.rawQuery(sqlQuery, selectionArgs).use(handler))
            }
        }

    override fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?): Completable =
        Completable.create {
            databaseLock.write {
                database.update(table, values, whereClause, whereArgs)
                it.onComplete()
            }
        }

    override fun executeInsert(table: String, values: ContentValues?): Completable =
        Completable.create {
            databaseLock.write {
                database.insert(table, null, values)
                it.onComplete()
            }
        }

    override fun executeReplace(table: String, values: ContentValues?): Completable =
        Completable.create {
            databaseLock.write {
                database.replace(table, null, values)
                it.onComplete()
            }
        }

    override fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?): Completable =
        Completable.create {
            databaseLock.write {
                database.delete(table, whereClause, whereArgs)
                it.onComplete()
            }
        }
}
