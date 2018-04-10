package org.stepik.android.adaptive.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import io.reactivex.Maybe
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.data.db.operations.DatabaseOperations
import org.stepik.android.adaptive.data.db.structure.ExpDbStructure
import org.stepik.android.adaptive.data.model.LocalExpItem
import org.stepik.android.adaptive.data.model.WeekProgress
import org.stepik.android.adaptive.di.storage.StorageSingleton
import javax.inject.Inject

@StorageSingleton
class ExpDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<LocalExpItem>(databaseOperations), ExpDao {
    companion object {
        private const val FIELD_DAY = "day"
        private const val FIELD_WEEK = "week"
    }

    override fun getDbName() = ExpDbStructure.TABLE_NAME
    override fun getDefaultPrimaryColumn() = ExpDbStructure.Columns.SUBMISSION_ID
    override fun getDefaultPrimaryValue(persistentObject: LocalExpItem) = persistentObject.submissionId.toString()

    override fun getContentValues(persistentObject: LocalExpItem) = ContentValues().apply {
        put(ExpDbStructure.Columns.SUBMISSION_ID, persistentObject.submissionId)
        put(ExpDbStructure.Columns.EXP, persistentObject.exp)
    }

    override fun parsePersistentObject(cursor: Cursor) = LocalExpItem(
            exp          = cursor.getLong(cursor.getColumnIndex(ExpDbStructure.Columns.EXP)),
            submissionId = cursor.getLong(cursor.getColumnIndex(ExpDbStructure.Columns.SUBMISSION_ID))
    )

    override fun getExpItem(submissionId: Long): Maybe<LocalExpItem> {
        val sqlPrefix = "SELECT * FROM ${getDbName()} WHERE "

        return if (submissionId == -1L) {
            val sql = sqlPrefix +
                    "${ExpDbStructure.Columns.SUBMISSION_ID} <> 0 " + // submission id = 0 only for syncing
                    "ORDER BY ${ExpDbStructure.Columns.SOLVED_AT} DESC LIMIT 1"
            getAll(sql, null)
        } else {
            val sql = sqlPrefix + "${ExpDbStructure.Columns.SUBMISSION_ID} = ?"
            getAll(sql, arrayOf(submissionId.toString()))
        }.flatMapMaybe {
            val item = it.firstOrNull()
            if (item != null) {
                Maybe.just(item)
            } else {
                Maybe.empty()
            }
        }
    }

    override fun getExp(): Single<Long> =
        rawQuery("SELECT IFNULL(SUM(${ExpDbStructure.Columns.EXP}), 0) as ${ExpDbStructure.Columns.EXP} FROM ${getDbName()}", null) {
            it.moveToFirst()

            return@rawQuery if (!it.isAfterLast) {
                it.getLong(it.getColumnIndex(ExpDbStructure.Columns.EXP))
            } else 0
        }

    override fun getExpForLast7Days(): Single<Array<Long>> {
        val sql =
                "SELECT " +
                "STRFTIME('%Y %j', ${ExpDbStructure.Columns.SOLVED_AT}) as $FIELD_DAY, " +
                "STRFTIME('%s', ${ExpDbStructure.Columns.SOLVED_AT}) as ${ExpDbStructure.Columns.SOLVED_AT}, " +
                "SUM(${ExpDbStructure.Columns.EXP}) as ${ExpDbStructure.Columns.EXP} " +
                "FROM ${getDbName()} " +
                "WHERE ${ExpDbStructure.Columns.SOLVED_AT} >= (SELECT DATETIME('now', '-7 day')) " +
                "AND ${ExpDbStructure.Columns.SUBMISSION_ID} <> 0 " +
                "GROUP BY $FIELD_DAY " +
                "ORDER BY $FIELD_DAY"

        return rawQuery(sql, null) {
            val res = Array<Long>(7) { 0 }
            val now = DateTime.now().withTimeAtStartOfDay()

            if (it.moveToFirst()) {
                do {
                    val date = DateTime(it.getLong(it.getColumnIndex(ExpDbStructure.Columns.SOLVED_AT)) * 1000).withTimeAtStartOfDay()
                    val day = Days.daysBetween(date, now).days

                    if (day in 0..6) {
                        res[6 - day] = it.getLong(it.getColumnIndex(ExpDbStructure.Columns.EXP))
                    }
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
    }

    override fun getWeeks(): Single<List<WeekProgress>> {
        val sql =
                "SELECT " +
                "STRFTIME('%Y %W', ${ExpDbStructure.Columns.SOLVED_AT}) as $FIELD_WEEK, " +
                "STRFTIME('%s', ${ExpDbStructure.Columns.SOLVED_AT}) as ${ExpDbStructure.Columns.SOLVED_AT}, " +
                "SUM(${ExpDbStructure.Columns.EXP}) as ${ExpDbStructure.Columns.EXP} " +
                "FROM ${getDbName()} " +
                "WHERE ${ExpDbStructure.Columns.SUBMISSION_ID} <> 0 " +
                "GROUP BY $FIELD_WEEK " +
                "ORDER BY $FIELD_WEEK DESC"

        return rawQuery(sql, null) {
            val res = ArrayList<WeekProgress>()

            if (it.moveToFirst()) {
                do {
                    val w = it.getLong(it.getColumnIndex(ExpDbStructure.Columns.SOLVED_AT))
                    val dt = DateTime(w * 1000)
                    val start = dt.withDayOfWeek(1)
                    val end = dt.withDayOfWeek(7)

                    res.add(WeekProgress(start, end, it.getLong(it.getColumnIndex(ExpDbStructure.Columns.EXP))))
                } while (it.moveToNext())
            }

            return@rawQuery res
        }
    }


}