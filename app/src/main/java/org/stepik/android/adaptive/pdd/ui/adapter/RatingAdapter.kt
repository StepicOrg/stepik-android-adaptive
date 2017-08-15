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

class RatingAdapter : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {
    private val items = ArrayList<RatingItem>()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            RatingViewHolder(ItemRatingBinding.inflate(LayoutInflater.from(parent?.context), parent, false))

    override fun onBindViewHolder(holder: RatingViewHolder?, position: Int) {
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

    fun add(data: Collection<RatingItem>) {
        items.addAll(data)
        notifyDataSetChanged()
    }

    class RatingViewHolder(val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root)
}