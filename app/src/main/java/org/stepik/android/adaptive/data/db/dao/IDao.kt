package org.stepik.android.adaptive.data.db.dao

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface IDao<T> {
    fun insertOrUpdate(persistentObject: T): Completable
    fun insertOrReplace(persistentObject: T): Completable

    fun update(persistentObject: T): Completable

    fun isInDb(persistentObject: T): Single<Boolean>
    fun isInDb(whereColumn: String, value: String): Single<Boolean>

    fun getAll(): Single<List<T>>
    fun getAll(whereColumnName: String, whereValue: String): Single<List<T>>
    fun getAll(query: String, whereArgs: Array<String>?): Single<List<T>>
    fun getAllOrdered(orderBy: String, orderDirection: String = ""): Single<List<T>>

    fun get(whereColumnName: String, whereValue: String): Maybe<T>
    fun remove(whereColumn: String, whereValue: String): Completable
    fun remove(persistentObject: T): Completable
    fun removeAll(): Completable
}