package org.stepik.android.adaptive.core.presenter

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.core.presenter.contracts.BookmarksView
import org.stepik.android.adaptive.data.AnalyticMgr
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.ui.adapter.BookmarksAdapter

class BookmarksPresenter: PresenterBase<BookmarksView>() {
    companion object : PresenterFactory<BookmarksPresenter> {
        override fun create(): BookmarksPresenter = BookmarksPresenter()
    }

    private var isLoading = true
    private val adapter = BookmarksAdapter(::removeFromBookmarks)
    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
                Single.fromCallable {
                    DataBaseMgr.instance.getBookmarks()
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    adapter.addAll(it)
                    isLoading = false

                    view?.onStopLoading()
                    resolveBookmarksCount()
                }, {})
        )
    }

    private fun removeFromBookmarks(bookmark: Bookmark, pos: Int) {
        AnalyticMgr.getInstance().logEvent(AnalyticMgr.EVENT_ON_BOOKMARK_REMOVED)
        compositeDisposable.add(
                Completable.fromCallable {
                    DataBaseMgr.instance.removeBookmark(bookmark)
                }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe()
        )
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

    override fun destroy() {
        compositeDisposable.dispose()
    }
}