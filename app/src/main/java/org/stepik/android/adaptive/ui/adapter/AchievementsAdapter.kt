package org.stepik.android.adaptive.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemAchievementBinding
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import javax.inject.Inject


class AchievementsAdapter
@Inject
constructor(
        achievementManager: AchievementManager
): RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder>() {
    private val achievements = achievementManager.achievements

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AchievementViewHolder(ItemAchievementBinding.inflate(LayoutInflater.from(parent?.context), parent, false))

    override fun onBindViewHolder(holder: AchievementViewHolder, p: Int) {
        val context = holder.binding.root.context

        holder.binding.title.text = achievements[p].title
        holder.binding.description.text = achievements[p].description

        holder.binding.progressContainer.visibility = if (achievements[p].showProgress) View.VISIBLE else View.GONE

        holder.binding.progress.max = achievements[p].targetValue.toInt()
        holder.binding.progress.progress = achievements[p].currentValue.toInt()
        holder.binding.progressValues.text = context.getString(R.string.ach_progress_values, achievements[p].currentValue, achievements[p].targetValue)

        holder.binding.icon.alpha = if (achievements[p].isComplete()) 1f else 0.4f


        if (achievements[p].icon != -1) {
            holder.binding.icon.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(context, achievements[p].icon)!!))
        }
    }

    override fun getItemCount() = achievements.size


    class AchievementViewHolder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)
}