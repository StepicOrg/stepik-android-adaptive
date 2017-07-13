package org.stepik.android.adaptive.pdd.ui.adapter

import android.view.View

class OnboardingQuizCardsAdapter(private val onOnboardingEnd: () -> Unit) : QuizCardsAdapter(null, null) {

    override fun onBindViewHolder(holder: QuizCardViewHolder, pos: Int) {
        super.onBindViewHolder(holder, pos)
        holder.binding.fragmentRecommendationsAnswers?.visibility = View.GONE
        holder.binding.fragmentRecommendationsQuestion?.setOnWebViewClickListener(null)
    }

    override fun onBindTopCard(holder: QuizCardViewHolder?, pos: Int) {
        super.onBindTopCard(holder, pos)
        holder?.binding?.fragmentRecommendationsSubmit?.visibility = View.GONE
        when (getItemCount()) {
            4, 1 -> {
                holder?.binding?.fragmentRecommendationsNext?.visibility = View.VISIBLE
                holder?.binding?.fragmentRecommendationsContainer?.isEnabled = false
            }

        }
    }

    override fun poll() {
        super.poll()
        if (getItemCount() == 0)
            onOnboardingEnd()
    }
}