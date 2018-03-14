package org.stepik.android.adaptive.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.stepik.android.adaptive.data.db.structure.ExpDbStructure

class DataBaseHelper(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "stepik_adaptive_pdd"
        private const val DB_VERSION = 1
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE ${ExpDbStructure.TABLE_NAME} (" +
                "${ExpDbStructure.Columns.EXP} INTEGER," +
                "${ExpDbStructure.Columns.SOLVED_AT} DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "${ExpDbStructure.Columns.SUBMISSION_ID} INTEGER," +
                "PRIMARY KEY (${ExpDbStructure.Columns.SOLVED_AT}, ${ExpDbStructure.Columns.SUBMISSION_ID}));")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}