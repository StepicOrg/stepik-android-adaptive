package org.stepik.android.adaptive.pdd.ui.adapter

import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.model.WeekProgress
import org.stepik.android.adaptive.pdd.databinding.HeaderStatsBinding
import org.stepik.android.adaptive.pdd.databinding.ItemWeekBinding
import java.util.*


class WeeksAdapter : RecyclerView.Adapter<WeeksAdapter.StatsViewHolder>() {
    data class Header(var total: Long = 0, var level: Long = 0, var last7Days: Long = 0, var chartData: LineDataSet? = null)

    companion object {
        private val HEADER_VIEW_TYPE = 1
        private val ITEM_VIEW_TYPE = 2

        private val DATE_FORMAT = "dd MMMM yyyy"
    }

    private val weeks = ArrayList<WeekProgress>()
    private val header = Header()

    fun setHeaderLevelAndTotal(level: Long, total: Long) {
        header.level = level
        header.total = total
        notifyItemChanged(0)
    }

    fun setHeaderChart(chartData: LineDataSet?, last7Days: Long) {
        header.chartData = chartData
        header.last7Days = last7Days
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER_VIEW_TYPE else ITEM_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : StatsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == HEADER_VIEW_TYPE) {
            StatsViewHolder.StatsHeaderViewHolder(DataBindingUtil.inflate(inflater, R.layout.header_stats, parent, false))
        } else {
            StatsViewHolder.WeekViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_week, parent, false))
        }
    }



    override fun onBindViewHolder(holder: StatsViewHolder?, p: Int) {
        holder?.let {
            when (it) {
                is StatsViewHolder.WeekViewHolder -> {
                    it.binding.total.text = weeks[p - 1].total.toString()
                    it.binding.start.text = weeks[p - 1].start.toString(DATE_FORMAT, Resources.getSystem().configuration.locale)
                    it.binding.end.text = weeks[p - 1].end.toString(DATE_FORMAT, Resources.getSystem().configuration.locale)
                }
                is StatsViewHolder.StatsHeaderViewHolder -> {
                    val binding = it.binding
                    header.chartData?.let { dataSet ->
                        dataSet.color = ContextCompat.getColor(binding.root.context, R.color.colorAccent)
                        dataSet.setDrawCircles(false)
                        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        dataSet.cubicIntensity = 0.2f
                        dataSet.fillColor = dataSet.color
                        dataSet.fillAlpha = 100
                        dataSet.setDrawValues(true)
                        dataSet.setValueFormatter { v, _, _, _ -> v.toLong().toString() }
                        dataSet.valueTextSize = 12f
                        dataSet.setDrawHorizontalHighlightIndicator(false)

                        dataSet.setDrawCircles(true)
                        dataSet.setCircleColor(dataSet.color)

                        binding.chart.data = LineData(dataSet)
                        binding.chart.data.isHighlightEnabled = true

                        if (dataSet.entryCount > 0) {
                            binding.chart.animateY(1400)
                            binding.chart.invalidate()
                            binding.chart.visibility = View.VISIBLE
                        } else {
                            binding.chart.visibility = View.GONE
                        }
                    }
                    binding.chart.visibility = if (header.chartData == null) View.INVISIBLE else View.VISIBLE

                    binding.expTotal.text = header.total.toString()
                    binding.level.text = header.level.toString()
                    binding.expThisWeek.text = header.last7Days.toString()
                }
            }
        }
    }

    override fun getItemCount() = weeks.size + 1

    fun addAll(data: List<WeekProgress>) {
        weeks.addAll(data)
        notifyDataSetChanged()
    }

    sealed class StatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class WeekViewHolder(val binding: ItemWeekBinding) : StatsViewHolder(binding.root)
        class StatsHeaderViewHolder(val binding: HeaderStatsBinding) : StatsViewHolder(binding.root) {
            init {
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
            }
        }
    }
}