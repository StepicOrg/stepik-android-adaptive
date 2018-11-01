package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_paid_content.view.*
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.gamification.InventoryManager

class PaidInventoryAdapter(
        private val purchase: (Sku, InventoryManager.PaidContent) -> Unit
) : RecyclerView.Adapter<PaidInventoryAdapter.PaidContentViewHolder>() {
    var items: List<Pair<Sku, InventoryManager.PaidContent>> = emptyList()
        set(value) {
            field = value.sortedBy { it.first.detailedPrice.amount }
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PaidContentViewHolder, pos: Int) {
        holder.title.text = items[pos].first.displayTitle
        holder.description.text = items[pos].first.description
        holder.price.text = items[pos].first.price
        holder.icon.setImageResource(items[pos].second.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidContentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paid_content, parent, false)

        return PaidContentViewHolder(view)
    }

    private fun onItemClick(pos: Int) = items.getOrNull(pos)?.let { (sku, paidContent) ->
        purchase(sku, paidContent)
    }


    inner class PaidContentViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val title: TextView = root.title
        val price: TextView = root.price
        val description: TextView = root.description
        val icon: ImageView = root.icon

        init {
            root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }
}