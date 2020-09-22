package org.stepik.android.adaptive.core.presenter.contracts

import org.stepik.android.adaptive.ui.adapter.QuestionsPacksAdapter

interface QuestionsPacksView : PaidContentView {
    fun showAuthScreen()
    fun onAdapter(adapter: QuestionsPacksAdapter)
    fun onContentError()
}
