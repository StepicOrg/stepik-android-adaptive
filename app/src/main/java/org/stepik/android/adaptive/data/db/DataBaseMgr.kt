package org.stepik.android.adaptive.data.db

import android.content.ContentValues
import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.data.db.dao.BookmarksDao
import org.stepik.android.adaptive.data.db.operations.DatabaseOperationsImpl
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.db.structure.ExpDbStructure
import org.stepik.android.adaptive.data.model.WeekProgress
import org.stepik.android.adaptive.data.model.Bookmark

class DataBaseMgr private constructor(context: Context) {
    companion object {
        @JvmStatic
        lateinit var instance: DataBaseMgr

        @JvmStatic
        fun init(context: Context) {
            instance = DataBaseMgr(context)
        }
    }

    private val db = DataBaseHelper(context).writableDatabase
    private val databaseOperations = DatabaseOperationsImpl(db)
    private val bookmarksDao = BookmarksDao(databaseOperations) // todo replace with DI

    fun onExpGained(exp: Long, submissionId: Long) {
        val cv = ContentValues()

        cv.put(ExpDbStructure.Columns.EXP, exp)
        cv.put(ExpDbStructure.Columns.SUBMISSION_ID, submissionId)

        db.insert(ExpDbStructure.TABLE_NAME, null, cv)
    }

    fun getExpForLast7Days(): Array<Long> {
        val res = Array<Long>(7) { 0 }

        val FIELD_DAY = "day"

        val cursor = db.query(
                ExpDbStructure.TABLE_NAME, // TABLE
                arrayOf("strftime('%Y %j', ${ExpDbStructure.Columns.SOLVED_AT}) as $FIELD_DAY", // SELECT
                        "strftime('%s', ${ExpDbStructure.Columns.SOLVED_AT}) as ${ExpDbStructure.Columns.SOLVED_AT}",
                        "sum(${ExpDbStructure.Columns.EXP}) as ${ExpDbStructure.Columns.EXP}"
                ),
                "${ExpDbStructure.Columns.SOLVED_AT} >= (SELECT DATETIME('now', '-7 day'))", // WHERE
                null, // WHERE ARGS
                FIELD_DAY, // GROUP BY
                null, // having
                FIELD_DAY // ORDER BY
        )

        cursor.use {
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
        }

        return res
    }

    fun getWeeks() : List<WeekProgress> {
        val res = ArrayList<WeekProgress>()

        val FIELD_WEEK = "week"

        val cursor = db.query(
                ExpDbStructure.TABLE_NAME,
                arrayOf(
                        "strftime('%Y %W', ${ExpDbStructure.Columns.SOLVED_AT}) as $FIELD_WEEK",
                        "strftime('%s', ${ExpDbStructure.Columns.SOLVED_AT}) as ${ExpDbStructure.Columns.SOLVED_AT}",
                        "sum(${ExpDbStructure.Columns.EXP}) as ${ExpDbStructure.Columns.EXP}"
                ),
                null,
                null,
                FIELD_WEEK,
                null,
                "$FIELD_WEEK DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val w = it.getLong(it.getColumnIndex(ExpDbStructure.Columns.SOLVED_AT))
                    val dt = DateTime(w * 1000)
                    val start = dt.withDayOfWeek(1)
                    val end = dt.withDayOfWeek(7)

                    res.add(WeekProgress(start, end, it.getLong(it.getColumnIndex(ExpDbStructure.Columns.EXP))))
                } while (it.moveToNext())
            }
        }

        return res
    }

    fun getExp(): Long {
        val cursor = db.query(
                ExpDbStructure.TABLE_NAME,
                arrayOf("sum(${ExpDbStructure.Columns.EXP}) as ${ExpDbStructure.Columns.EXP}"),
                null, null, null, null, null
        )

        var exp = -1L

        cursor.use {
            if (it.moveToFirst()) {
                exp = it.getLong(it.getColumnIndex(ExpDbStructure.Columns.EXP))
            }
        }

        return exp
    }

    fun addBookmark(bookmark: Bookmark) =
            bookmarksDao.insertOrReplace(bookmark)

    fun removeBookmark(bookmark: Bookmark) =
            bookmarksDao.remove(bookmark)

    fun getBookmarks() =
            bookmarksDao.getAll()

    fun updateBookmarks(bookmark: Bookmark) =
            bookmarksDao.update(bookmark)

    fun isInBookmarks(bookmark: Bookmark) =
            bookmarksDao.isInDb(bookmark)

    fun isInBookmarks(stepId: Long) =
            bookmarksDao.isInDb(BookmarksDbStructure.Columns.STEP_ID, stepId.toString())
}