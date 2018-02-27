package org.stepik.android.adaptive.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.item_word_pack.view.*
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.QuestionsPack
import org.stepik.android.adaptive.util.changeVisibillity

class QuestionsPacksAdapter(private val onPackClicked: (Sku, QuestionsPack, Boolean) -> Unit) : RecyclerView.Adapter<QuestionsPacksAdapter.QuestionsPackViewHolder>() {
    var items: List<Pair<Sku, QuestionsPack>> = emptyList()
        set(value) {
            field = value.sortedBy { it.second.ordinal }
            notifyDataSetChanged()
        }

    var selection: Int = 0
        set(value) {
            notifyItemChanged(field)
            field = value
            notifyItemChanged(value)
        }

    private val owned = HashSet<String>()

    fun addOwnedContent(ownedContent: List<String>) {
        owned.addAll(ownedContent)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            QuestionsPackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_word_pack, parent, false))

    override fun onBindViewHolder(holder: QuestionsPackViewHolder, pos: Int) {
        val (sku, pack) = items[pos]
        val isOwned = owned.contains(pack.id)
        holder.title.text = sku.displayTitle
        holder.price.text = sku.price
        holder.price.changeVisibillity(!(pack.isFree || isOwned))
        holder.description.text = sku.description
        holder.actionButton.setOnClickListener {
            onPackClicked(sku, pack, isOwned)
        }
        holder.actionButton.setText(if (pack.isFree || isOwned) {
            R.string.select
        } else {
            R.string.buy
        })
        holder.actionButton.isEnabled = pack.ordinal != selection
    }

    class QuestionsPackViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val title: TextView = root.packTitle
        val price: TextView = root.packPrice
        val description: TextView = root.packDescription
        val actionButton: Button = root.packButton
    }
}