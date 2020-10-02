package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import org.stepik.android.adaptive.core.presenter.contracts.BookmarksView
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.ui.adapter.BookmarksAdapter
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class BookmarksPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val dataBaseMgr: DataBaseMgr,
    private val analytics: Analytics
) : PresenterBase<BookmarksView>() {
    private var isLoading = true
    private val adapter = BookmarksAdapter(::removeFromBookmarks, analytics)
    init {
        compositeDisposable += dataBaseMgr.getBookmarks()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe(
                {
                    adapter.addAll(it)
                    isLoading = false

                    view?.onStopLoading()
                    resolveBookmarksCount()
                },
                {}
            )
    }

    private fun removeFromBookmarks(bookmark: Bookmark, pos: Int) {
        analytics.logEvent(Analytics.EVENT_ON_BOOKMARK_REMOVED)
        compositeDisposable += dataBaseMgr.removeBookmark(bookmark)
            .subscribeOn(backgroundScheduler).observeOn(backgroundScheduler).subscribe()
        adapter.remove(pos)
    }

    override fun attachView(view: BookmarksView) {
        super.attachView(view)
        view.onAdapter(adapter)
        if (isLoading) {
            view.onStartLoading()
        } else {
            view.onStopLoading()
            resolveBookmarksCount()
        }
    }

    private fun resolveBookmarksCount() {
        if (adapter.itemCount == 0) {
            view?.onNoBookmarks()
        }
    }
}
