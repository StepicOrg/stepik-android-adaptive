package org.stepik.android.adaptive.pdd.core.presenter.contracts

import com.github.mikephil.charting.data.LineDataSet
import org.stepik.android.adaptive.pdd.ui.adapter.WeeksAdapter

interface StatsView {
    fun onWeeksAdapter(adapter: WeeksAdapter)
    fun onChartData(dataSet: LineDataSet)
    fun onLast7Days(exp: Long)
    fun onTotal(total: Long)
    fun onLevel(level: Long)
}