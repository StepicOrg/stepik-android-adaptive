package org.stepik.android.adaptive.pdd.ui.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.pdd.core.presenter.PresenterFactory
import org.stepik.android.adaptive.pdd.core.presenter.RatingPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.pdd.databinding.RecyclerViewBinding
import org.stepik.android.adaptive.pdd.ui.adapter.RatingAdapter

class RatingFragment : BasePresenterFragment<RatingPresenter, RatingView>(), RatingView {
    private lateinit var recycler: RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = RecyclerViewBinding.inflate(inflater, container, false).recycler
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        return recycler
    }

    override fun onLoading() {

    }

    override fun onError() {
    }

    override fun onComplete() {
    }

    override fun onRatingAdapter(adapter: RatingAdapter) {
        recycler.adapter = adapter
    }


    private var presenter : RatingPresenter? = null

    override fun onPresenter(presenter: RatingPresenter) {
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

    override fun getPresenterFactory(): PresenterFactory<RatingPresenter> = RatingPresenter.Companion
}