package org.stepik.android.adaptive.data.db.operations

import android.content.ContentValues
import android.database.Cursor
import io.reactivex.Completable
import io.reactivex.Single

interface DatabaseOperations {
    fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: (Cursor) -> U): Single<U>

    fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?): Completable
    fun executeInsert(table: String, values: ContentValues?): Completable
    fun executeReplace(table: String, values: ContentValues?): Completable
    fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?): Completable
}
