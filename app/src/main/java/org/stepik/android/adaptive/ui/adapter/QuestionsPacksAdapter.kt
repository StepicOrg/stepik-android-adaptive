package org.stepik.android.adaptive.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_questions_pack.view.*
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.content.questions.QuestionsPacksResolver
import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.ui.helper.setAlpha
import org.stepik.android.adaptive.util.changeVisibillity

class QuestionsPacksAdapter(
        private val onPackClicked: (Sku, QuestionsPack, Boolean) -> Unit,
        private val questionsPacksResolver: QuestionsPacksResolver
) : RecyclerView.Adapter<QuestionsPacksAdapter.QuestionsPackViewHolder>() {
    companion object {
        private const val TITLE_ALPHA = 0xDD
        private const val TEXT_ALPHA = 0xCC
        private const val ICON_ALPHA = 0xFF
    }

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
            QuestionsPackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_questions_pack, parent, false))

    override fun onBindViewHolder(holder: QuestionsPackViewHolder, pos: Int) {
        val (sku, pack) = items[pos]
        val isOwned = owned.contains(pack.id)
        val context = holder.root.context

        holder.title.text = sku.displayTitle
        holder.title.setTextColor(setAlpha(pack.textColor, TITLE_ALPHA))

        holder.description.text = sku.description
        holder.description.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

        holder.questionsCount.text = Html.fromHtml(context.getString(R.string.questions_count, pack.size))
        holder.questionsCount.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

        holder.difficulty.text = Html.fromHtml(context.getString(R.string.questions_difficulty, context.getString(pack.difficulty)))
        holder.difficulty.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

        val activeDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_correct))
        DrawableCompat.setTint(activeDrawable, setAlpha(pack.textColor, ICON_ALPHA))
        holder.activeIcon.setImageDrawable(activeDrawable)
        holder.activeIcon.changeVisibillity(pack.ordinal == selection)

        holder.actionButton.changeVisibillity(pack.ordinal != selection)
        holder.actionButton.setOnClickListener {
            onPackClicked(sku, pack, isOwned)
        }
        holder.actionButton.text = (if (questionsPacksResolver.isAvailableForFree(pack) || isOwned) {
            context.getString(R.string.select)
        } else {
            sku.price
        })
        holder.root.setBackgroundResource(pack.background)
    }

    class QuestionsPackViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val title: TextView = root.packTitle
        val questionsCount: TextView = root.packQuestionsCount
        val difficulty: TextView = root.packDifficulty
        val description: TextView = root.packDescription
        val actionButton: Button = root.packButton
        val activeIcon: ImageView = root.packActiveIcon
        val root: View = root.cardBody
    }
}