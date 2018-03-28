package org.stepik.android.adaptive.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.db.structure.ExpDbStructure
import org.stepik.android.adaptive.di.storage.StorageSingleton
import javax.inject.Inject

@StorageSingleton
class DataBaseHelper
@Inject
constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "stepik_adaptive_pdd"
        private const val DB_VERSION = 2
    }


    override fun onCreate(db: SQLiteDatabase) {
        createExpDatabase(db)

        upgradeFrom1To2(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            upgradeFrom1To2(db)
        }
    }

    private fun upgradeFrom1To2(db: SQLiteDatabase) {
        createBookmarksDatabase(db)
    }

    private fun createBookmarksDatabase(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ${BookmarksDbStructure.TABLE_NAME} (" +
                "${BookmarksDbStructure.Columns.STEP_ID} INTEGER, " +
                "${BookmarksDbStructure.Columns.COURSE_ID} INTEGER, " +
                "${BookmarksDbStructure.Columns.TITLE} TEXT, " +
                "${BookmarksDbStructure.Columns.DEFINITION} TEXT, " +
                "${BookmarksDbStructure.Columns.DATE_ADDED} DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (${BookmarksDbStructure.Columns.STEP_ID}));")
    }

    private fun createExpDatabase(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ${ExpDbStructure.TABLE_NAME} (" +
                "${ExpDbStructure.Columns.EXP} INTEGER," +
                "${ExpDbStructure.Columns.SOLVED_AT} DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "${ExpDbStructure.Columns.SUBMISSION_ID} INTEGER," +
                "PRIMARY KEY (${ExpDbStructure.Columns.SOLVED_AT}, ${ExpDbStructure.Columns.SUBMISSION_ID}));")
    }
}