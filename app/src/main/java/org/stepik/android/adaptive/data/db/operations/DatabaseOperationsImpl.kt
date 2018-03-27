package org.stepik.android.adaptive.data.db.operations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.util.RWLocks

class DatabaseOperationsImpl(private val database: SQLiteDatabase): DatabaseOperations {
    private fun open() {
        RWLocks.DatabaseLock.writeLock().lock()
    }

    private fun close() {
        RWLocks.DatabaseLock.writeLock().unlock()
    }

    private fun openRead() {
        RWLocks.DatabaseLock.readLock().lock()
    }

    private fun closeRead() {
        RWLocks.DatabaseLock.readLock().unlock()
    }

    override fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: (Cursor) -> U): Single<U> = Single.create { emitter ->
        try {
            openRead()
            emitter.onSuccess(database.rawQuery(sqlQuery, selectionArgs).use(handler))
        } finally {
            closeRead()
        }
    }

    override fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?): Completable = Completable.create {
        try {
            open()
            database.update(table, values, whereClause, whereArgs)
            it.onComplete()
        } finally {
            close()
        }
    }

    override fun executeInsert(table: String, values: ContentValues?): Completable = Completable.create {
        try {
            open()
            database.insert(table, null, values)
            it.onComplete()
        } finally {
            close()
        }
    }

    override fun executeReplace(table: String, values: ContentValues?): Completable = Completable.create {
        try {
            open()
            database.replace(table, null, values)
            it.onComplete()
        } finally {
            close()
        }
    }

    override fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?): Completable = Completable.create {
        try {
            open()
            database.delete(table, whereClause, whereArgs)
            it.onComplete()
        } finally {
            close()
        }
    }
}