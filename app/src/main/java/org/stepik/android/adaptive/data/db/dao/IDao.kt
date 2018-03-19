package org.stepik.android.adaptive.data.db.dao

interface IDao<T> {
    fun insertOrUpdate(persistentObject: T)
    fun insertOrReplace(persistentObject: T)

    fun update(persistentObject: T)

    fun isInDb(persistentObject: T): Boolean
    fun isInDb(whereColumn: String, value: String): Boolean

    fun getAll(): List<T>
    fun getAll(whereColumnName: String, whereValue: String): List<T>
    fun getAll(query: String, whereArgs: Array<String>?): List<T>

    fun get(whereColumnName: String, whereValue: String): T?
    fun remove(whereColumn: String, whereValue: String)
    fun remove(persistentObject: T)
    fun removeAll()
}