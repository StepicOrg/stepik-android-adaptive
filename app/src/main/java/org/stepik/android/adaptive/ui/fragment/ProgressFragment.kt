package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.core.presenter.ProgressPresenter
import org.stepik.android.adaptive.core.presenter.contracts.ProgressView
import org.stepik.android.adaptive.ui.adapter.WeeksAdapter
import javax.inject.Inject
import javax.inject.Provider


class ProgressFragment : BasePresenterFragment<ProgressPresenter, ProgressView>(), ProgressView {
    private lateinit var recycler : RecyclerView

    @Inject
    lateinit var progressPresenterProvider: Provider<ProgressPresenter>

    override fun injectComponent() {
        App.componentManager()
                .statsComponent
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = inflater.inflate(R.layout.recycler_view, container, false).recycler
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        return recycler
    }

    override fun onWeeksAdapter(adapter: WeeksAdapter) {
       recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = progressPresenterProvider
}