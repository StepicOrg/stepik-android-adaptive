package org.stepik.android.adaptive.arch.view.question_packs.ui.adapter

import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.item_questions_pack.view.*
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.arch.domain.question_packs.model.EnrollmentState
import org.stepik.android.adaptive.arch.domain.question_packs.model.QuestionListItem
import org.stepik.android.adaptive.ui.helper.setAlpha
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.fromHtmlCompat
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class QuestionPackAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onPackClicked: (Sku?, QuestionListItem, Boolean) -> Unit
) : AdapterDelegate<QuestionListItem, DelegateViewHolder<QuestionListItem>>() {
    companion object {
        private const val TITLE_ALPHA = 0xDD
        private const val TEXT_ALPHA = 0xCC
        private const val ICON_ALPHA = 0xFF
    }

    override fun isForViewType(position: Int, data: QuestionListItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<QuestionListItem> =
        ViewHolder(createView(parent, R.layout.item_questions_pack))

    private inner class ViewHolder(root: View) : DelegateViewHolder<QuestionListItem>(root) {
        private val title: TextView = root.packTitle
        private val questionsCount: TextView = root.packQuestionsCount
        private val difficulty: TextView = root.packDifficulty
        private val description: TextView = root.packDescription
        private val actionButton: Button = root.packButton
        private val activeIcon: ImageView = root.packActiveIcon
        private val progressDescription: TextView = root.packProgressDescription
        private val root: View = root.cardBody

        private val packPriceDiscount: TextView = root.packPriceDiscount
        private val packPriceDiscountDescription: TextView = root.packPriceDiscountDescription

        init {
            packPriceDiscount.paintFlags = packPriceDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        override fun onBind(data: QuestionListItem) {
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            val pack = data.questionPack
            val sku = (data.enrollmentState as? EnrollmentState.NotEnrolledInApp)?.skuWrapper?.sku
            val isOwned = data.enrollmentState == EnrollmentState.Enrolled
            val context = root.context

            title.text = pack.name
            title.setTextColor(setAlpha(pack.textColor, TITLE_ALPHA))

            description.text = data.course.summary
            description.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

            questionsCount.text = fromHtmlCompat(context.getString(R.string.questions_count, data.course.totalUnits))
            questionsCount.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

            difficulty.text = fromHtmlCompat(context.getString(R.string.questions_difficulty, context.getString(pack.difficulty)))
            difficulty.setTextColor(setAlpha(pack.textColor, TEXT_ALPHA))

            val activeDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.ic_correct)!!)
            DrawableCompat.setTint(activeDrawable, setAlpha(pack.textColor, ICON_ALPHA))
            activeIcon.setImageDrawable(activeDrawable)
            activeIcon.changeVisibillity(itemView.isSelected)

            actionButton.changeVisibillity(!itemView.isSelected)
            actionButton.setOnClickListener {
                onPackClicked(sku, data, isOwned)
            }

            packPriceDiscount.changeVisibillity(false)
            packPriceDiscountDescription.changeVisibillity(false)

            if (isOwned || !data.course.isPaid) {
                actionButton.setText(R.string.select)
            } else {
                actionButton.text = sku?.price
            }
            root.setBackgroundResource(pack.background)

            progressDescription.changeVisibillity(false)
        }
    }
}
