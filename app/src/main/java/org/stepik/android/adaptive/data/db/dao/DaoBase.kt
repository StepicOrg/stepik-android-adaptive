package org.stepik.android.adaptive.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.adaptive.data.db.operations.DatabaseOperations

abstract class DaoBase<T>(private val databaseOperations: DatabaseOperations) : IDao<T> {
    override fun insertOrUpdate(persistentObject: T): Completable {
        val primaryValue = getDefaultPrimaryValue(persistentObject)
        val primaryKey = getDefaultPrimaryColumn()
        val cv = getContentValues(persistentObject)
        return isInDb(primaryKey, primaryValue).flatMapCompletable { inDb ->
            if (inDb) {
                databaseOperations.executeUpdate(getDbName(), cv, "$primaryKey = ?", arrayOf(primaryValue))
            } else {
                databaseOperations.executeInsert(getDbName(), cv)
            }
        }
    }

    override fun insertOrReplace(persistentObject: T): Completable =
        databaseOperations.executeReplace(getDbName(), getContentValues(persistentObject))

    override fun update(persistentObject: T): Completable =
        databaseOperations.executeUpdate(
            getDbName(),
            getContentValues(persistentObject),
            "${getDefaultPrimaryColumn()} = ?",
            arrayOf(getDefaultPrimaryValue(persistentObject))
        )

    override fun isInDb(persistentObject: T): Single<Boolean> =
        isInDb(getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject))

    override fun isInDb(whereColumn: String, value: String): Single<Boolean> =
        databaseOperations.executeQuery("SELECT * FROM ${getDbName()} WHERE $whereColumn = ?", arrayOf(value)) {
            it.count > 0
        }

    override fun getAll(): Single<List<T>> =
        getAll("SELECT * FROM ${getDbName()}", null)

    override fun getAll(whereColumnName: String, whereValue: String): Single<List<T>> =
        getAll("SELECT * FROM ${getDbName()} WHERE $whereColumnName = ?", arrayOf(whereValue))

    override fun getAll(query: String, whereArgs: Array<String>?): Single<List<T>> =
        databaseOperations.executeQuery(query, whereArgs) { cursor ->
            val data = ArrayList<T>()

            if (cursor.moveToFirst()) {
                do {
                    data.add(parsePersistentObject(cursor))
                } while (cursor.moveToNext())
            }

            return@executeQuery data
        }

    override fun getAllOrdered(orderBy: String, orderDirection: String): Single<List<T>> =
        getAll("SELECT * FROM ${getDbName()} ORDER BY $orderBy $orderDirection", null)

    override fun get(whereColumnName: String, whereValue: String): Maybe<T> =
        getAll("SELECT * FROM ${getDbName()} WHERE $whereColumnName = ? COUNT 1", arrayOf(whereValue)).flatMapMaybe {
            val item = it.firstOrNull()
            if (item != null) {
                Maybe.just(item)
            } else {
                Maybe.empty()
            }
        }

    override fun remove(whereColumn: String, whereValue: String): Completable =
        databaseOperations.executeDelete(getDbName(), "$whereColumn = ?", arrayOf(whereValue))

    override fun remove(persistentObject: T): Completable =
        remove(getDefaultPrimaryColumn(), getDefaultPrimaryValue(persistentObject))

    override fun removeAll(): Completable =
        databaseOperations.executeDelete(getDbName(), null, null)

    protected fun <U> rawQuery(query: String, whereArgs: Array<String>?, handler: (Cursor) -> U): Single<U> =
        databaseOperations.executeQuery(query, whereArgs, handler)

    protected abstract fun getDbName(): String
    protected abstract fun getDefaultPrimaryColumn(): String
    protected abstract fun getDefaultPrimaryValue(persistentObject: T): String
    protected abstract fun getContentValues(persistentObject: T): ContentValues
    protected abstract fun parsePersistentObject(cursor: Cursor): T
}
