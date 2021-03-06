package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_rating.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.*
import org.stepik.android.adaptive.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.ui.adapter.RatingAdapter
import javax.inject.Inject
import javax.inject.Provider

class RatingFragment : BasePresenterFragment<RatingPresenter, RatingView>(), RatingView {
    @Inject
    lateinit var ratingPresenterProvider: Provider<RatingPresenter>

    override fun injectComponent() {
        App.componentManager()
                .statsComponent
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_rating, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        val spinnerAdapter = ArrayAdapter<CharSequence>(context, R.layout.item_rating_period, context.resources.getStringArray(R.array.rating_periods))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                presenter?.changeRatingPeriod(pos)
            }
        }

        tryAgain.setOnClickListener { presenter?.retry() }
    }

    override fun onLoading() {
        error.visibility = View.GONE
        progress.visibility = View.VISIBLE
        container.visibility = View.GONE
    }

    private fun onError() {
        error.visibility = View.VISIBLE
        progress.visibility = View.GONE
        container.visibility = View.GONE
    }

    override fun onConnectivityError() {
        errorMessage.setText(R.string.connectivity_error)
        onError()
    }

    override fun onRequestError() {
        errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onComplete() {
        error.visibility = View.GONE
        progress.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    override fun onRatingAdapter(adapter: RatingAdapter) {
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

    override fun getPresenterProvider() = ratingPresenterProvider
}