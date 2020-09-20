package org.stepik.android.adaptive.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemDailyRewardBinding
import org.stepik.android.adaptive.gamification.InventoryManager

class DailyRewardsAdapter(private val rewards: List<List<Pair<InventoryManager.Item, Int>>>) : RecyclerView.Adapter<DailyRewardsAdapter.DailyRewardViewHolder>() {
    var currentProgress: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() =
        rewards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DailyRewardViewHolder(ItemDailyRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DailyRewardViewHolder, position: Int) {
        holder.let {
            it.binding.day.text = it.itemView.context.getString(R.string.day_number, position + 1)
            (it.binding.rewards.adapter as InventoryAdapter).data = rewards[position]
            it.binding.root.isSelected = currentProgress == position
            if (position < currentProgress) {
                it.binding.root.alpha = 0.6f
            } else {
                it.binding.root.alpha = 1f
            }
        }
    }

    class DailyRewardViewHolder(val binding: ItemDailyRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rewards.isNestedScrollingEnabled = false
            binding.rewards.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rewards.adapter = InventoryAdapter(emptyList())
        }
    }
}
