package org.stepik.android.adaptive.pdd.ui.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.model.Achievement
import org.stepik.android.adaptive.pdd.databinding.ItemAchievementBinding


class AchievementsAdapter : RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {
    private val achievements = ArrayList<Achievement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AchievementViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_achievement, parent, false))


    override fun onBindViewHolder(holder: AchievementViewHolder?, p: Int) {
        holder?.binding?.let {
            it.icon.setImageResource(achievements[p].iconId)
            it.title.setText(achievements[p].titleId)
            it.description.setText(achievements[p].descriptionId)
        }
    }

    override fun getItemCount() = achievements.size

    fun addAll(data: List<Achievement>) {
        achievements.addAll(data)
        notifyDataSetChanged()
    }

    class AchievementViewHolder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)
}