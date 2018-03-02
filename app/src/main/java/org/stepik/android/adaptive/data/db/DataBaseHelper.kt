package org.stepik.android.adaptive.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "stepik_adaptive_pdd"
        private const val DB_VERSION = 1

        const val TABLE_EXP = "exp"
        const val FIELD_EXP = "exp_delta"
        const val FIELD_SOLVED_AT = "solved_at"
        const val FIELD_SUBMISSION_ID = "submission_id"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_EXP (" +
                "$FIELD_EXP INTEGER," +
                "$FIELD_SOLVED_AT DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "$FIELD_SUBMISSION_ID INTEGER," +
                "PRIMARY KEY ($FIELD_SOLVED_AT, $FIELD_SUBMISSION_ID));")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}