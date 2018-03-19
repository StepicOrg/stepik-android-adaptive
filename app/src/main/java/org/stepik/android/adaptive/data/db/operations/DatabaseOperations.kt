package org.stepik.android.adaptive.data.db.operations

import android.content.ContentValues
import android.database.Cursor

interface DatabaseOperations {
    fun <U> executeQuery(sqlQuery: String?, selectionArgs: Array<String>?, handler: (Cursor) -> U): U

    fun executeUpdate(table: String, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?)
    fun executeInsert(table: String, values: ContentValues?)
    fun executeReplace(table: String, values: ContentValues?)
    fun executeDelete(table: String, whereClause: String?, whereArgs: Array<String>?)
}