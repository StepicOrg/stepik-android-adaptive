package org.stepik.android.adaptive.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BookmarksPresenter
import org.stepik.android.adaptive.core.presenter.contracts.BookmarksView
import org.stepik.android.adaptive.ui.adapter.BookmarksAdapter
import org.stepik.android.adaptive.util.changeVisibillity
import javax.inject.Inject

class BookmarksFragment: Fragment(), BookmarksView {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: BookmarksPresenter

    private fun injectComponent() {
        App.componentManager().statsComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()
        super.onCreate(savedInstanceState)
        presenter = ViewModelProvider(this, viewModelFactory).get(BookmarksPresenter::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_bookmarks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.stroke)!!)
        recycler.addItemDecoration(divider)
    }

    override fun onAdapter(adapter: BookmarksAdapter) {
        recycler.adapter = adapter
    }

    override fun onStartLoading() {
        recycler.changeVisibillity(false)
        progress.changeVisibillity(true)
        noContent.changeVisibillity(false)
    }

    override fun onStopLoading() {
        recycler.changeVisibillity(true)
        progress.changeVisibillity(false)
        noContent.changeVisibillity(false)
    }

    override fun onNoBookmarks() {
        recycler.changeVisibillity(false)
        progress.changeVisibillity(false)
        noContent.changeVisibillity(true)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }
}