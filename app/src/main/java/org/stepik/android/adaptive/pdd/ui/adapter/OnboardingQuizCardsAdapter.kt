package org.stepik.android.adaptive.pdd.ui.adapter

import android.view.View

class OnboardingQuizCardsAdapter(private val onOnboardingEnd: () -> Unit) : QuizCardsAdapter(null, null) {

    override fun onBindViewHolder(holder: QuizCardViewHolder, pos: Int) {
        super.onBindViewHolder(holder, pos)
        holder.binding.answers.visibility = View.GONE
        holder.binding.question.setOnWebViewClickListener(null)
    }

    override fun onBindTopCard(holder: QuizCardViewHolder, pos: Int) {
        super.onBindTopCard(holder, pos)
        holder.binding.submit.visibility = View.GONE

        holder.binding.separatorAnswers.visibility = View.GONE
        holder.binding.separatorHint.visibility = View.GONE

        when (getItemCount()) {
            4, 1 -> {
                holder.binding.next.visibility = View.VISIBLE
                holder.binding.container.isEnabled = false
            }

        }
    }

    override fun poll() {
        super.poll()
        if (getItemCount() == 0)
            onOnboardingEnd()
    }
}