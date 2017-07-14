package org.stepik.android.adaptive.pdd.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.pdd.core.presenter.PresenterFactory
import org.stepik.android.adaptive.pdd.core.presenter.StatsPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.StatsView
import org.stepik.android.adaptive.pdd.data.model.Achievement
import org.stepik.android.adaptive.pdd.databinding.ActivityStatsBinding
import org.stepik.android.adaptive.pdd.ui.adapter.AchievementsAdapter


class StatsActivity : BasePresenterActivity<StatsPresenter, StatsView>(), StatsView {
    private var presenter : StatsPresenter? = null

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_down, R.anim.fade_in)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_stats)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.achievements)

        val adapter = AchievementsAdapter() // some stubs to display
        adapter.addAll(listOf(
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false),
                Achievement(1, R.drawable.ic_correct, R.string.rate_app_title, R.string.rate_app_message, false)

        ))

        binding.achievements.adapter = adapter
        binding.achievements.layoutManager = LinearLayoutManager(this)

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.stroke))
        binding.achievements.addItemDecoration(divider)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_up)
    }

    override fun onPresenter(presenter: StatsPresenter) {
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

    override fun getPresenterFactory(): PresenterFactory<StatsPresenter> = StatsPresenter.Companion
}