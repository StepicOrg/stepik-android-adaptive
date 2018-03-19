package org.stepik.android.adaptive.data.db.operations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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

    override fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: (Cursor) -> U): U {
        try {
            openRead()
            return database.rawQuery(sqlQuery, selectionArgs).use(handler)
        } finally {
            closeRead()
        }
    }

    override fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?) {
        try {
            open()
            database.update(table, values, whereClause, whereArgs)
        } finally {
            close()
        }
    }

    override fun executeInsert(table: String, values: ContentValues?) {
        try {
            open()
            database.insert(table, null, values)
        } finally {
            close()
        }
    }

    override fun executeReplace(table: String, values: ContentValues?) {
        try {
            open()
            database.replace(table, null, values)
        } finally {
            close()
        }
    }

    override fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?) {
        try {
            open()
            database.delete(table, whereClause, whereArgs)
        } finally {
            close()
        }
    }
}