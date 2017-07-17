package org.stepik.android.adaptive.pdd.ui.adapter

import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.joda.time.DateTime
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.model.WeekProgress
import org.stepik.android.adaptive.pdd.databinding.ItemWeekBinding
import java.util.*


class WeeksAdapter : RecyclerView.Adapter<WeeksAdapter.WeekViewHolder>() {
    private val weeks = ArrayList<WeekProgress>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            WeekViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_week, parent, false))


    override fun onBindViewHolder(holder: WeekViewHolder?, p: Int) {
        holder?.binding?.let {
            it.total.text = weeks[p].total.toString()


            val week = DateTime.now().minusWeeks(p)

            it.start.text = week.withDayOfWeek(1).toString("dd MMMM yyyy", Resources.getSystem().configuration.locale)
            it.end.text = week.withDayOfWeek(7).toString("dd MMMM yyyy", Resources.getSystem().configuration.locale)
        }
    }

    override fun getItemCount() = weeks.size

    fun addAll(data: List<WeekProgress>) {
        weeks.addAll(data)
        notifyDataSetChanged()
    }

    class WeekViewHolder(val binding: ItemWeekBinding) : RecyclerView.ViewHolder(binding.root)
}