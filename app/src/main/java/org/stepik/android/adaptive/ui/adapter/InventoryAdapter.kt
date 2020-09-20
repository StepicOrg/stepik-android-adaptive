package org.stepik.android.adaptive.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemInventoryBinding
import org.stepik.android.adaptive.gamification.InventoryManager

class InventoryAdapter(private var _data: List<Pair<InventoryManager.Item, Int>>) : RecyclerView.Adapter<InventoryAdapter.InventoryItemViewHolder>() {
    var data: List<Pair<InventoryManager.Item, Int>>
        get() = _data
        set(value) {
            _data = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int =
        data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder =
        InventoryItemViewHolder(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder?.let {
            val context = it.binding.root.context

            it.binding.icon.setImageResource(data[position].first.iconId)
            it.binding.counter.text = context.getString(R.string.amount, data[position].second)
        }
    }

    class InventoryItemViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)
}
