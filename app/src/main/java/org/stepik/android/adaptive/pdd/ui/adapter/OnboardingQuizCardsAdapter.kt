package org.stepik.android.adaptive.pdd.ui.adapter

import android.view.View

class OnboardingQuizCardsAdapter(private val onOnboardingEnd: () -> Unit) : QuizCardsAdapter(null, null) {

    override fun onBindViewHolder(holder: QuizCardViewHolder?, pos: Int) {
        super.onBindViewHolder(holder, pos)
        holder?.binding?.fragmentRecommendationsAnswers?.visibility = View.GONE
    }

    override fun onBindTopCard(holder: QuizCardViewHolder?, pos: Int) {
        super.onBindTopCard(holder, pos)
        holder?.binding?.fragmentRecommendationsSubmit?.visibility = View.GONE
        when (itemCount) {
            4, 1 -> {
                holder?.binding?.fragmentRecommendationsNext?.visibility = View.VISIBLE
                holder?.binding?.fragmentRecommendationsContainer?.isEnabled = false
            }

        }
    }

    override fun poll() {
        super.poll()
        if (itemCount == 0)
            onOnboardingEnd()
    }
}