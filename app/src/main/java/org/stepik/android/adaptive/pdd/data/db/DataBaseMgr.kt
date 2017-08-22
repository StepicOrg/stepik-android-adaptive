package org.stepik.android.adaptive.pdd.data.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.Days
import org.stepik.android.adaptive.pdd.data.model.WeekProgress
import org.stepik.android.adaptive.pdd.util.ExpUtil

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

    fun onExpGained(exp: Long, submissionId: Long) {
        val cv = ContentValues()

        cv.put(DataBaseHelper.FIELD_EXP, exp)
        cv.put(DataBaseHelper.FIELD_SUBMISSION_ID, submissionId)

        db.insert(DataBaseHelper.TABLE_EXP, null, cv)
    }

    fun getExpForLast7Days(): Array<Long> {
        val res = Array<Long>(7) { 0 }

        val FIELD_DAY = "day"

        val cursor = db.query(
                DataBaseHelper.TABLE_EXP, // TABLE
                arrayOf("strftime('%Y %j', ${DataBaseHelper.FIELD_SOLVED_AT}) as $FIELD_DAY", // SELECT
                        "strftime('%s', ${DataBaseHelper.FIELD_SOLVED_AT}) as ${DataBaseHelper.FIELD_SOLVED_AT}",
                        "sum(${DataBaseHelper.FIELD_EXP}) as ${DataBaseHelper.FIELD_EXP}"
                ),
                "${DataBaseHelper.FIELD_SOLVED_AT} >= (SELECT DATETIME('now', '-7 day'))", // WHERE
                null, // WHERE ARGS
                FIELD_DAY, // GROUP BY
                null, // having
                FIELD_DAY // ORDER BY
        )

        cursor.use {
            val now = DateTime.now().withTimeAtStartOfDay()

            if (it.moveToFirst()) {
                do {
                    val date = DateTime(it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_SOLVED_AT)) * 1000).withTimeAtStartOfDay()
                    val day = Days.daysBetween(date, now).days

                    if (day in 0..6) {
                        res[6 - day] = it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_EXP))
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
                DataBaseHelper.TABLE_EXP,
                arrayOf(
                        "strftime('%Y %W', ${DataBaseHelper.FIELD_SOLVED_AT}) as $FIELD_WEEK",
                        "strftime('%s', ${DataBaseHelper.FIELD_SOLVED_AT}) as ${DataBaseHelper.FIELD_SOLVED_AT}",
                        "sum(${DataBaseHelper.FIELD_EXP}) as ${DataBaseHelper.FIELD_EXP}"
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
                    val w = it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_SOLVED_AT))
                    val dt = DateTime(w * 1000)
                    val start = dt.withDayOfWeek(1)
                    val end = dt.withDayOfWeek(7)

                    res.add(WeekProgress(start, end, it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_EXP))))
                } while (it.moveToNext())
            }
        }

        return res
    }

    fun getExp(): Long {
        val cursor = db.query(
                DataBaseHelper.TABLE_EXP,
                arrayOf("sum(${DataBaseHelper.FIELD_EXP}) as ${DataBaseHelper.FIELD_EXP}"),
                null, null, null, null, null
        )

        var exp = -1L

        cursor.use {
            if (it.moveToFirst()) {
                exp = it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_EXP))
            }
        }

        return exp
    }

    fun getStreak(): Long {
        var exp = ExpUtil.getStreak()
        if (exp == 0L) return exp

        val cursor = db.query(
                DataBaseHelper.TABLE_EXP,
                arrayOf(DataBaseHelper.FIELD_EXP),
                null, null, null, null,
                "${DataBaseHelper.FIELD_SOLVED_AT} DESC",
                "1"
        )

        cursor.use {
            if (it.moveToFirst()) {
                exp = it.getLong(it.getColumnIndex(DataBaseHelper.FIELD_EXP))
            }
        }

        return exp
    }
}