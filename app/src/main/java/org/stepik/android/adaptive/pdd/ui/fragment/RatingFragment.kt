package org.stepik.android.adaptive.pdd.ui.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.pdd.core.presenter.PresenterFactory
import org.stepik.android.adaptive.pdd.core.presenter.RatingPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.pdd.databinding.FragmentRatingBinding
import org.stepik.android.adaptive.pdd.ui.adapter.RatingAdapter

class RatingFragment : BasePresenterFragment<RatingPresenter, RatingView>(), RatingView {
    private lateinit var binding: FragmentRatingBinding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRatingBinding.inflate(inflater, container, false)
        binding.recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        binding.recycler.addItemDecoration(divider)

        val spinnerAdapter = ArrayAdapter<CharSequence>(context, R.layout.item_rating_period, context.resources.getStringArray(R.array.rating_periods))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                presenter?.changeRatingPeriod(pos)
            }
        }

        binding.tryAgain.setOnClickListener { presenter?.retry() }
        return binding.root
    }

    override fun onLoading() {
        binding.error.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
        binding.container.visibility = View.GONE
    }

    private fun onError() {
        binding.error.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
        binding.container.visibility = View.GONE
    }

    override fun onConnectivityError() {
        binding.errorMessage.setText(R.string.connectivity_error)
        onError()
    }

    override fun onRequestError() {
        binding.errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onComplete() {
        binding.error.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.container.visibility = View.VISIBLE
    }

    override fun onRatingAdapter(adapter: RatingAdapter) {
        binding.recycler.adapter = adapter
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