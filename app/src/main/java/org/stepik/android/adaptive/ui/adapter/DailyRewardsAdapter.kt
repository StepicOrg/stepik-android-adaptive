package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemDailyRewardBinding
import org.stepik.android.adaptive.gamification.InventoryManager


class DailyRewardsAdapter(private val rewards: List<List<Pair<InventoryManager.Item, Int>>>) : RecyclerView.Adapter<DailyRewardsAdapter.DailyRewardViewHolder>() {
    var currentProgress: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = rewards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DailyRewardViewHolder(ItemDailyRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DailyRewardViewHolder, position: Int) {
        with(holder) {
            binding.day.text = itemView.context.getString(R.string.day_number, position + 1)
            (binding.rewards.adapter as InventoryAdapter).data = rewards[position]
            binding.root.isSelected = currentProgress == position
            if (position < currentProgress) {
                binding.root.alpha = 0.6f
            } else {
                binding.root.alpha = 1f
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