package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemInventoryBinding
import org.stepik.android.adaptive.gamification.InventoryManager

class InventoryAdapter(private var _data: List<Pair<InventoryManager.Item, Int>>) : RecyclerView.Adapter<InventoryAdapter.InventoryItemViewHolder>() {
     var data : List<Pair<InventoryManager.Item, Int>>
        get() = _data
        set(value) {
            _data = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InventoryItemViewHolder(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        val context = holder.binding.root.context

        holder.binding.icon.setImageResource(data[position].first.iconId)
        holder.binding.counter.text = context.getString(R.string.amount, data[position].second)
    }


    class InventoryItemViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)
}