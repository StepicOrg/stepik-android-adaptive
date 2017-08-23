package org.stepik.android.adaptive.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ItemInventoryBinding
import org.stepik.android.adaptive.util.InventoryUtil

class InventoryAdapter(private var _data: List<Pair<InventoryUtil.Item, Int>>) : RecyclerView.Adapter<InventoryAdapter.InventoryItemViewHolder>() {
     var data : List<Pair<InventoryUtil.Item, Int>>
        get() = _data
        set(value) {
            _data = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            InventoryItemViewHolder(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: InventoryItemViewHolder?, position: Int) {
        holder?.let {
            val context = it.binding.root.context

            val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, data[position].first.iconId))
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.colorDarkGrayText))
            it.binding.icon.setImageDrawable(drawable)

            it.binding.counter.text = context.getString(R.string.amount, data[position].second)
        }
    }


    class InventoryItemViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)
}