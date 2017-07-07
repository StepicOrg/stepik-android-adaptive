package org.stepik.android.adaptive.pdd.ui.adapter

import android.support.design.widget.Snackbar
import android.view.View
import org.stepik.android.adaptive.pdd.core.presenter.CardPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.CardView
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction
import org.stepik.android.adaptive.pdd.data.model.Submission
import org.stepik.android.adaptive.pdd.databinding.QuizCardViewBinding
import org.stepik.android.adaptive.pdd.ui.helper.AnimationHelper
import org.stepik.android.adaptive.pdd.ui.helper.CardHelper
import org.stepik.android.adaptive.pdd.ui.view.QuizCardView
import org.stepik.android.adaptive.pdd.ui.view.QuizCardsContainer
import org.stepik.android.adaptive.pdd.util.HtmlUtil
import android.support.v7.widget.LinearLayoutManager
import android.webkit.WebSettings
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.core.ScreenManager
import org.stepik.android.adaptive.pdd.ui.DefaultWebViewClient

class QuizCardViewHolder(val binding: QuizCardViewBinding) : QuizCardsContainer.CardViewHolder(binding.root), CardView {
    init {
        val settings = binding.fragmentRecommendationsQuestion.settings
        settings.allowContentAccess = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL

        binding.fragmentRecommendationsQuestion.setWebViewClient(DefaultWebViewClient(null) { _, _ -> onCardLoaded() })
        binding.fragmentRecommendationsQuestion.setOnWebViewClickListener { path -> ScreenManager.showImage(binding.root.context, path) }
        binding.fragmentRecommendationsQuestion.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        binding.fragmentRecommendationsAnswers.isNestedScrollingEnabled = false
        binding.fragmentRecommendationsAnswers.layoutManager = LinearLayoutManager(binding.root.context)

        binding.fragmentRecommendationsNext.setOnClickListener { binding.fragmentRecommendationsContainer.swipeDown() }
        binding.fragmentRecommendationsSubmit.setOnClickListener { presenter?.createSubmission() }
        binding.fragmentRecommendationsWrongRetry.setOnClickListener {
            presenter?.let {
                it.retrySubmission()
                CardHelper.resetSupplementalActions(binding)
            }
        }

        binding.fragmentRecommendationsContainer.visibility = View.INVISIBLE // to hide loading of web view
    }

    private var hasSubmission = false
    private var presenter: CardPresenter? = null

    fun bind(presenter: CardPresenter) {
        this.presenter = presenter
        presenter.attachView(this)
    }

    fun onTopCard() {
        if (!hasSubmission) {
            if (presenter?.isLoading ?: false) {
                onSubmissionLoading()
                binding.fragmentRecommendationsAnswersProgress.visibility = View.VISIBLE
            } else {
                binding.fragmentRecommendationsSubmit.visibility = View.VISIBLE
                (binding.fragmentRecommendationsAnswers.adapter as AttemptAnswersAdapter).setEnabled(true)
            }
        }

        binding.fragmentRecommendationsContainer.setQuizCardFlingListener(object : QuizCardView.QuizCardFlingListener() {
            override fun onScroll(scrollProgress: Float) {
                binding.fragmentRecommendationsHardReaction.alpha = Math.max(2 * scrollProgress, 0f)
                binding.fragmentRecommendationsEasyReaction.alpha = Math.max(2 * -scrollProgress, 0f)
            }

            override fun onSwipeLeft() {
                binding.fragmentRecommendationsEasyReaction.alpha = 1f
                presenter?.createReaction(RecommendationReaction.Reaction.NEVER_AGAIN)
            }

            override fun onSwipeRight() {
                binding.fragmentRecommendationsHardReaction.alpha = 1f
                presenter?.createReaction(RecommendationReaction.Reaction.MAYBE_LATER)
            }
        })
    }

    private fun onCardLoaded() {
        binding.fragmentRecommendationsContainer.visibility = View.VISIBLE
        if (!(presenter?.isLoading ?: false)) binding.fragmentRecommendationsAnswersProgress.visibility = View.GONE

        CardHelper.scrollDown(binding.fragmentRecommendationsScroll)
    }

    override fun setTitle(title: String) {
        binding.fragmentRecommendationsTitle.text = title
    }

    override fun setQuestion(html: String) {
        HtmlUtil.setCardWebViewHtml(
                binding.fragmentRecommendationsQuestion,
                HtmlUtil.prepareCardHtml(html))
    }

    override fun setAnswerAdapter(adapter: AttemptAnswersAdapter) {
        binding.fragmentRecommendationsAnswers.adapter = adapter
        adapter.setSubmitButton(binding.fragmentRecommendationsSubmit)
        adapter.setEnabled(false)

        binding.fragmentRecommendationsSubmit.visibility = View.GONE
    }

    override fun setSubmission(submission: Submission, animate: Boolean) {
        CardHelper.resetSupplementalActions(binding)
        when (submission.status) {
            Submission.Status.CORRECT -> {
                binding.fragmentRecommendationsSubmit.visibility = View.GONE
                hasSubmission = true

                binding.fragmentRecommendationsCorrect.visibility = View.VISIBLE
                binding.fragmentRecommendationsNext.visibility = View.VISIBLE
                binding.fragmentRecommendationsContainer.isEnabled = true

                binding.fragmentRecommendationsHint.text = submission.hint
                binding.fragmentRecommendationsHint.visibility = View.VISIBLE

                if (animate) {
                    CardHelper.scrollDown(binding.fragmentRecommendationsScroll)
                }
            }

            Submission.Status.WRONG -> {
                binding.fragmentRecommendationsWrong.visibility = View.VISIBLE
                hasSubmission = true

                binding.fragmentRecommendationsWrongRetry.visibility = View.VISIBLE
                binding.fragmentRecommendationsSubmit.visibility = View.GONE

                binding.fragmentRecommendationsContainer.isEnabled = true

                if (animate) {
                    AnimationHelper.playWiggleAnimation(binding.fragmentRecommendationsContainer)
                }
            }
        }
    }

    override fun onSubmissionError() {
        Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_SHORT).show()
        CardHelper.resetSupplementalActions(binding)
    }

    override fun onSubmissionLoading() {
        CardHelper.resetSupplementalActions(binding)
        binding.fragmentRecommendationsContainer.isEnabled = false
        binding.fragmentRecommendationsSubmit.visibility = View.GONE
        binding.fragmentRecommendationsAnswersProgress.visibility = View.VISIBLE

        CardHelper.scrollDown(binding.fragmentRecommendationsScroll)
    }
}