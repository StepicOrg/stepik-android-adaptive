package org.stepik.android.adaptive.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.RatingItem
import org.stepik.android.adaptive.databinding.ItemRatingBinding

class RatingAdapter(
    private val profileId: Long
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var leaderIconDrawable: Drawable
    private lateinit var leaderIconDrawableSelected: Drawable

    private companion object {
        private const val RATING_ITEM_VIEW_TYPE = 1
        private const val SEPARATOR_VIEW_TYPE = 2

        private const val SEPARATOR = -1L

        @JvmStatic
        private fun isRatingGap(current: RatingItem, next: RatingItem) =
            current.rank + 1 != next.rank

        @JvmStatic
        private fun isNotSeparatorStub(item: RatingItem) =
            item.user != SEPARATOR
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context = recyclerView.context

        leaderIconDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_crown)!!)
        DrawableCompat.setTint(leaderIconDrawable, ContextCompat.getColor(context, R.color.colorYellow))

        leaderIconDrawableSelected = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_crown)!!)
        DrawableCompat.setTint(leaderIconDrawableSelected, ContextCompat.getColor(context, android.R.color.white))
    }

    private val items = ArrayList<RatingItem>()

    override fun getItemViewType(position: Int) =
        if (isNotSeparatorStub(items[position])) {
            RATING_ITEM_VIEW_TYPE
        } else {
            SEPARATOR_VIEW_TYPE
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            RATING_ITEM_VIEW_TYPE -> RatingViewHolder(ItemRatingBinding.inflate(LayoutInflater.from(parent?.context), parent, false))
            SEPARATOR_VIEW_TYPE -> SeparatorViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.ranks_separator, parent, false))
            else -> throw IllegalStateException("Unknown view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            RATING_ITEM_VIEW_TYPE -> {
                (holder as RatingViewHolder).binding.let {
                    it.rank.text = items[position].rank.toString()
                    it.exp.text = items[position].exp.toString()
                    it.name.text = items[position].name

                    it.root.isSelected = profileId == items[position].user

                    if (items[position].rank == 1) {
                        it.icon.setImageDrawable(
                            if (profileId == items[position].user) {
                                leaderIconDrawableSelected
                            } else {
                                leaderIconDrawable
                            }
                        )

                        it.icon.visibility = View.VISIBLE
                        it.rank.visibility = View.GONE
                    } else {
                        it.icon.visibility = View.GONE
                        it.rank.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun set(data: Collection<RatingItem>) {
        items.clear()
        items.addAll(data)
        addSeparator()
        notifyDataSetChanged()
    }

    private fun addSeparator() {
        (items.size - 2 downTo 0)
            .filter { isRatingGap(items[it], items[it + 1]) && isNotSeparatorStub(items[it]) && isNotSeparatorStub(items[it + 1]) }
            .forEach { items.add(it + 1, RatingItem(0, "", 0, SEPARATOR)) }
    }

    class RatingViewHolder(val binding: ItemRatingBinding) : RecyclerView.ViewHolder(binding.root)
    class SeparatorViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
