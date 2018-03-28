package org.stepik.android.adaptive.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepik.android.adaptive.data.db.operations.DatabaseOperations
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.di.storage.StorageSingleton
import javax.inject.Inject

@StorageSingleton
class BookmarksDao
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<Bookmark>(databaseOperations) {
    override fun getDbName() = BookmarksDbStructure.TABLE_NAME

    override fun getDefaultPrimaryColumn() = BookmarksDbStructure.Columns.STEP_ID

    override fun getDefaultPrimaryValue(persistentObject: Bookmark) = persistentObject.stepId.toString()

    override fun getContentValues(persistentObject: Bookmark) = ContentValues().apply {
        put(BookmarksDbStructure.Columns.COURSE_ID, persistentObject.courseId)
        put(BookmarksDbStructure.Columns.STEP_ID, persistentObject.stepId)
        put(BookmarksDbStructure.Columns.TITLE, persistentObject.title)
        put(BookmarksDbStructure.Columns.DEFINITION, persistentObject.definition)
    }

    override fun parsePersistentObject(cursor: Cursor) = Bookmark(
            cursor.getLong(cursor.getColumnIndex(BookmarksDbStructure.Columns.COURSE_ID)),
            cursor.getLong(cursor.getColumnIndex(BookmarksDbStructure.Columns.STEP_ID)),
            cursor.getString(cursor.getColumnIndex(BookmarksDbStructure.Columns.TITLE)),
            cursor.getString(cursor.getColumnIndex(BookmarksDbStructure.Columns.DEFINITION))
    )
}