package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.BookmarksAdapter

interface BookmarksView {
    fun onAdapter(adapter: BookmarksAdapter)
    fun onStartLoading()
    fun onStopLoading()
    fun onNoBookmarks()
}