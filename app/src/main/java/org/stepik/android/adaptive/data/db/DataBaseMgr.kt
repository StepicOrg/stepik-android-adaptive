package org.stepik.android.adaptive.data.db

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.data.db.dao.ExpDao
import org.stepik.android.adaptive.data.db.dao.IDao
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.data.model.LocalExpItem
import org.stepik.android.adaptive.di.storage.StorageSingleton
import org.stepik.android.adaptive.util.then
import javax.inject.Inject

@StorageSingleton
class DataBaseMgr
@Inject
constructor(
        private val bookmarksDao: IDao<Bookmark>,
        private val expDao: ExpDao
) {
    fun onExpGained(exp: Long, submissionId: Long): Completable =
            expDao.insertOrReplace(LocalExpItem(exp, submissionId))

    fun getExpForLast7Days() = expDao.getExpForLast7Days()
    fun getWeeks() = expDao.getWeeks()
    fun getExp() = expDao.getExp()

    fun syncExp(apiExp: Long): Single<Long> = getExp().flatMap { localExp ->
        val diff = apiExp - localExp
        if (diff > 0) {
            val syncRecord = expDao.getExpItem(0).blockingGet()
            val exp = syncRecord?.exp ?: 0
            expDao.insertOrReplace(LocalExpItem(exp + diff, 0, solvedAt = syncRecord?.solvedAt)) then getExp()
        } else {
            Single.just(localExp)
        }
    }

    fun addBookmark(bookmark: Bookmark) =
            bookmarksDao.insertOrReplace(bookmark)

    fun removeBookmark(bookmark: Bookmark) =
            bookmarksDao.remove(bookmark)

    fun getBookmarks() =
            bookmarksDao.getAllOrdered(BookmarksDbStructure.Columns.DATE_ADDED, "DESC")

    fun updateBookmark(bookmark: Bookmark) =
            bookmarksDao.update(bookmark)

    fun isInBookmarks(bookmark: Bookmark) =
            bookmarksDao.isInDb(bookmark)

    fun isInBookmarks(stepId: Long) =
            bookmarksDao.isInDb(BookmarksDbStructure.Columns.STEP_ID, stepId.toString())
}