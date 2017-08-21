package org.stepik.android.adaptive.pdd.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.databinding.ItemAchievementBinding
import org.stepik.android.adaptive.pdd.util.AchievementManager


class AchievementsAdapter : RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {
    private val achievements = AchievementManager.achievements

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            AchievementViewHolder(ItemAchievementBinding.inflate(LayoutInflater.from(parent?.context), parent, false))

    override fun onBindViewHolder(holder: AchievementViewHolder, p: Int) {
        holder.binding.title.text = achievements[p].title
        holder.binding.description.text = achievements[p].description

        holder.binding.progress.visibility = if (achievements[p].showProgress) View.VISIBLE else View.GONE

        holder.binding.progress.max = achievements[p].targetValue.toInt()
        holder.binding.progress.progress = achievements[p].currentValue.toInt()

        holder.binding.root.alpha = if (achievements[p].isComplete()) 1f else 0.6f
    }

    override fun getItemCount() = achievements.size


    class AchievementViewHolder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)
}