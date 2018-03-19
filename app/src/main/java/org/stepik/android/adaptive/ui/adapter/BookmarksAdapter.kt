package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.data.model.Bookmark

class BookmarksAdapter : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class BookmarkViewHolder(root: View): RecyclerView.ViewHolder(root)
}