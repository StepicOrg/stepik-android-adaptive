package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.core.presenter.PresenterFactory
import org.stepik.android.adaptive.core.presenter.ProgressPresenter
import org.stepik.android.adaptive.core.presenter.contracts.ProgressView
import org.stepik.android.adaptive.databinding.RecyclerViewBinding
import org.stepik.android.adaptive.ui.adapter.WeeksAdapter


class ProgressFragment : BasePresenterFragment<ProgressPresenter, ProgressView>(), ProgressView {
    private var presenter : ProgressPresenter? = null

    private lateinit var recycler : RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = RecyclerViewBinding.inflate(inflater, container, false).recycler
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        return recycler
    }

    override fun onWeeksAdapter(adapter: WeeksAdapter) {
       recycler.adapter = adapter
    }

    override fun onPresenter(presenter: ProgressPresenter) {
        this.presenter = presenter
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterFactory(): PresenterFactory<ProgressPresenter> = ProgressPresenter.Companion
}