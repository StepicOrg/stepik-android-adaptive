package org.stepik.android.adaptive.data.db

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.data.db.dao.ExpDao
import org.stepik.android.adaptive.data.db.dao.IDao
import org.stepik.android.adaptive.data.db.structure.BookmarksDbStructure
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.data.model.LocalExpItem
import org.stepik.android.adaptive.data.model.WeekProgress
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

    fun getExpForLast7Days(): Single<Array<Long>> =
        expDao.getExpForLast7Days()

    fun getWeeks(): Single<List<WeekProgress>> =
        expDao.getWeeks()

    fun getExp(): Single<Long> =
        expDao.getExp()

    fun syncExp(apiExp: Long): Single<Long> =
        getExp().flatMap { localExp ->
            val diff = apiExp - localExp
            if (diff > 0) {
                val syncRecord = expDao.getExpItem(0).blockingGet()
                val exp = syncRecord?.exp ?: 0
                expDao.insertOrReplace(LocalExpItem(exp + diff, 0, solvedAt = syncRecord?.solvedAt)) then getExp()
            } else {
                Single.just(localExp)
            }
        }

    fun resetExp(): Completable =
        expDao.removeAll()

    fun addBookmark(bookmark: Bookmark): Completable =
        bookmarksDao.insertOrReplace(bookmark)

    fun removeBookmark(bookmark: Bookmark): Completable =
        bookmarksDao.remove(bookmark)

    fun getBookmarks(): Single<List<Bookmark>> =
        bookmarksDao.getAllOrdered(BookmarksDbStructure.Columns.DATE_ADDED, "DESC")

    fun updateBookmark(bookmark: Bookmark): Completable =
        bookmarksDao.update(bookmark)

    fun isInBookmarks(bookmark: Bookmark): Single<Boolean> =
        bookmarksDao.isInDb(bookmark)

    fun isInBookmarks(stepId: Long): Single<Boolean> =
        bookmarksDao.isInDb(BookmarksDbStructure.Columns.STEP_ID, stepId.toString())
}
