package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_bookmark.view.*
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.model.Bookmark
import org.stepik.android.adaptive.util.changeVisibillity

class BookmarksAdapter(
        private val removeBookmark: (Bookmark, Int) -> Unit,
        private val analytics: Analytics
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {
    private val data = ArrayList<Bookmark>()

    fun addAll(bookmarks: List<Bookmark>) {
        data.addAll(bookmarks)
        notifyDataSetChanged()
    }

    fun remove(pos: Int) {
        data.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BookmarkViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
    )

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = data[position]
        holder.title.text = bookmark.title
        holder.definition.text = bookmark.definition

        val isCorrect = bookmark.definition.isNotEmpty()
        holder.definition.changeVisibillity(isCorrect)
        holder.correct.changeVisibillity(isCorrect)
    }

    inner class BookmarkViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val title: TextView = root.title
        val definition: TextView = root.definition
        val correct: View = root.correct

        init {
            root.remove.setOnClickListener {
                removeBookmark(data[adapterPosition], adapterPosition)
            }

            root.setOnClickListener {
                analytics.logEvent(Analytics.EVENT_ON_BOOKMARK_CLICKED)
            }
        }
    }
}