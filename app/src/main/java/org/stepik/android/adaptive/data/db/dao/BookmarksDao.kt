package org.stepik.android.adaptive.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepik.android.adaptive.data.db.operations.DatabaseOperations
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.model.WordBookmark

class BookmarksDao(databaseOperations: DatabaseOperations): DaoBase<WordBookmark>(databaseOperations) {
    override fun getDbName() = BookmarksDbStructure.TABLE_NAME

    override fun getDefaultPrimaryColumn() = BookmarksDbStructure.Columns.STEP_ID

    override fun getDefaultPrimaryValue(persistentObject: WordBookmark) = persistentObject.stepId.toString()

    override fun getContentValues(persistentObject: WordBookmark) = ContentValues().apply {
        put(BookmarksDbStructure.Columns.COURSE_ID, persistentObject.courseId)
        put(BookmarksDbStructure.Columns.STEP_ID, persistentObject.stepId)
        put(BookmarksDbStructure.Columns.WORD, persistentObject.word)
        put(BookmarksDbStructure.Columns.DEFINITION, persistentObject.definition)
    }

    override fun parsePersistentObject(cursor: Cursor) = WordBookmark(
            cursor.getLong(cursor.getColumnIndex(BookmarksDbStructure.Columns.COURSE_ID)),
            cursor.getLong(cursor.getColumnIndex(BookmarksDbStructure.Columns.STEP_ID)),
            cursor.getString(cursor.getColumnIndex(BookmarksDbStructure.Columns.WORD)),
            cursor.getString(cursor.getColumnIndex(BookmarksDbStructure.Columns.DEFINITION))
    )
}