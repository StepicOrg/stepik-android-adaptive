package org.stepik.android.adaptive.pdd.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr
import org.stepik.android.adaptive.pdd.data.model.RatingItem
import org.stepik.android.adaptive.pdd.databinding.ItemRatingBinding

class RatingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private val RATING_ITEM_VIEW_TYPE = 1
        private val SEPARATOR_VIEW_TYPE = 2

        private val SEPARATOR = -1L
    }

    private val items = ArrayList<RatingItem>()

    override fun getItemViewType(position: Int) =
        if (items[position].user != SEPARATOR) {
            RATING_ITEM_VIEW_TYPE
        } else {
            SEPARATOR_VIEW_TYPE
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            if (viewType == RATING_ITEM_VIEW_TYPE) {
                RatingViewHolder(ItemRatingBinding.inflate(LayoutInflater.from(parent?.context), parent, false))
            } else {
                SeparatorViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.ranks_separator, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is RatingViewHolder?) {
            holder?.binding?.let {
                it.rank.text = items[position].rank.toString()
                it.exp.text = items[position].exp.toString()
                it.name.text = items[position].name

                it.root.isSelected = SharedPreferenceMgr.getInstance().profileId == items[position].user

                if (items[position].rank == 1) {

                    val context = it.root.context
                    val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_crown))
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(context, if (SharedPreferenceMgr.getInstance().profileId == items[position].user) {
                        android.R.color.white
                    } else {
                        R.color.colorYellow
                    }))

                    it.icon.setImageDrawable(drawable)

                    it.icon.visibility = View.VISIBLE
                    it.rank.visibility = View.GONE
                } else {
                    it.icon.visibility = View.GONE
                    it.rank.visibility = View.VISIBLE
                }
            }
        }
    }

    fun add(data: Collection<RatingItem>) {
        items.addAll(data)
        addSeparator()
        notifyDataSetChanged()
    }

    private fun addSeparator() {
        (items.size - 2 downTo 0)
                .filter { items[it].rank + 1 != items[it + 1].rank && items[it].user != SEPARATOR && items[it + 1].user != SEPARATOR }
                .forEach { items.add(it + 1, RatingItem(0, "", 0, SEPARATOR)) }
    }

    class RatingViewHolder(val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root)
    class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view)
}