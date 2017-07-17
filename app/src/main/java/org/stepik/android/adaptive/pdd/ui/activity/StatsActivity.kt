package org.stepik.android.adaptive.pdd.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.pdd.core.presenter.PresenterFactory
import org.stepik.android.adaptive.pdd.core.presenter.StatsPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.StatsView
import org.stepik.android.adaptive.pdd.data.model.WeekProgress
import org.stepik.android.adaptive.pdd.databinding.ActivityStatsBinding
import org.stepik.android.adaptive.pdd.ui.adapter.WeeksAdapter
import org.stepik.android.adaptive.pdd.util.ExpUtil


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

        val adapter = WeeksAdapter() // some stubs to display
        adapter.addAll(listOf( // todo mock
                WeekProgress(0, 0, listOf(124L)),
                WeekProgress(0, 0, listOf(1123L)),
                WeekProgress(0, 0, listOf(111L)),
                WeekProgress(0, 0, listOf(766L)),
                WeekProgress(0, 0, listOf(344L)),
                WeekProgress(0, 0, listOf(124L)),
                WeekProgress(0, 0, listOf(1344L)),
                WeekProgress(0, 0, listOf(124L)),
                WeekProgress(0, 0, listOf(34L)),
                WeekProgress(0, 0, listOf(134L)),
                WeekProgress(0, 0, listOf(24L)),
                WeekProgress(0, 0, listOf(124L)),
                WeekProgress(0, 0, listOf(334L)),
                WeekProgress(0, 0, listOf(121L)),
                WeekProgress(0, 0, listOf(94L)),
                WeekProgress(0, 0, listOf(224L))
        ))

        binding.weeks.adapter = adapter
        binding.weeks.layoutManager = LinearLayoutManager(this)

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.stroke))
        binding.weeks.addItemDecoration(divider)

        val exp = ExpUtil.getExp()

        binding.expTotal.text = exp.toString()
        binding.level.text = ExpUtil.getCurrentLevel(exp).toString()

        binding.expThisWeek.text = Util.getRandomNumberBetween(0, exp.toInt()).toString() // todo mock

        initChart()
    }

    private fun initChart() {
        val entries = ArrayList<Entry>()
        (0..7).mapTo(entries) { Entry(it.toFloat(), Util.getRandomNumberBetween(50, 500).toFloat()) } // todo mock

        val dataSet = LineDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(this, R.color.colorAccent)
        dataSet.setDrawCircles(false)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.cubicIntensity = 0.2f
        dataSet.fillColor = dataSet.color
        dataSet.fillAlpha = 100
        dataSet.setDrawValues(true)
        dataSet.setDrawHorizontalHighlightIndicator(false)

        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(dataSet.color)


        binding.chart.data = LineData(dataSet)
        binding.chart.data.isHighlightEnabled = true

        binding.chart.description.isEnabled = false
        binding.chart.setTouchEnabled(false)
        binding.chart.setScaleEnabled(false)
        binding.chart.setPinchZoom(false)
        binding.chart.setDrawGridBackground(false)
        binding.chart.isDragEnabled = false

        binding.chart.xAxis.isEnabled = false
        binding.chart.axisLeft.isEnabled = false
        binding.chart.axisRight.isEnabled = false

        binding.chart.legend.isEnabled = false

        binding.chart.invalidate()
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