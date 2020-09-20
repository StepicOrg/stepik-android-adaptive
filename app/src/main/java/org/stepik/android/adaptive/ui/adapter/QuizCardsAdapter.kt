package org.stepik.android.adaptive.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.CardPresenter
import org.stepik.android.adaptive.data.model.Card
import org.stepik.android.adaptive.databinding.QuizCardViewBinding
import org.stepik.android.adaptive.ui.listener.AdaptiveReactionListener
import org.stepik.android.adaptive.ui.listener.AnswerListener
import org.stepik.android.adaptive.ui.view.QuizCardsContainer
import java.util.ArrayList

open class QuizCardsAdapter(
    private val listener: AdaptiveReactionListener?,
    private val answerListener: AnswerListener?
) : QuizCardsContainer.CardsAdapter<QuizCardViewHolder>() {

    companion object {
        @JvmStatic
        private fun changeVisibilityOfAllChildrenTo(viewGroup: ViewGroup, visibility: Int, exclude: List<Int>?) {
            val count = viewGroup.childCount
            (0 until count)
                .map { viewGroup.getChildAt(it) }
                .filterNot { exclude != null && exclude.contains(it.id) }
                .forEach { it.visibility = visibility }
        }
    }

    private val presenters = ArrayList<CardPresenter>()

    fun destroy() {
        presenters.forEach(CardPresenter::destroy)
    }

    /**
     * Method that detaches adapter from container
     */
    fun detach() {
        container = null // detach from container
        presenters.forEach(CardPresenter::detachView)
    }

    fun isCardExists(lessonId: Long): Boolean =
        presenters.any { it.card.lessonId == lessonId }

    override fun onCreateViewHolder(parent: ViewGroup): QuizCardViewHolder =
        QuizCardViewHolder(QuizCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int =
        presenters.size

    override fun onBindViewHolder(holder: QuizCardViewHolder, pos: Int) {
        holder.bind(presenters[pos])
    }

    override fun onBindTopCard(holder: QuizCardViewHolder, pos: Int) {
        holder.onTopCard()
    }

    override fun onPositionChanged(holder: QuizCardViewHolder, pos: Int) {
        val p = holder.binding.card.layoutParams as FrameLayout.LayoutParams
        if (pos > 1) {
            p.height = QuizCardsContainer.CARD_OFFSET * 2
            changeVisibilityOfAllChildrenTo(holder.binding.card, View.GONE, listOf(R.id.curtain))
        } else {
            p.height = FrameLayout.LayoutParams.MATCH_PARENT
            changeVisibilityOfAllChildrenTo(holder.binding.card, View.VISIBLE, listOf(R.id.curtain))
        }
        holder.binding.card.layoutParams = p
    }

    fun add(card: Card) {
        presenters.add(CardPresenter(card, listener, answerListener))
        onDataAdded()
    }

    fun isEmptyOrContainsOnlySwipedCard(lesson: Long): Boolean =
        presenters.isEmpty() || presenters.size == 1 && presenters[0].card.lessonId == lesson

    override fun poll() {
        presenters.removeAt(0).destroy()
    }

    fun clear() {
        presenters.clear()
        onDataSetChanged()
    }
}
