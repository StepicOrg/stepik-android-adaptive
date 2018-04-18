package org.stepik.android.adaptive.ui.adapter

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import org.stepik.android.adaptive.core.presenter.CardPresenter
import org.stepik.android.adaptive.core.presenter.contracts.CardView
import org.stepik.android.adaptive.data.model.RecommendationReaction
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.databinding.QuizCardViewBinding
import org.stepik.android.adaptive.ui.animation.CardAnimations
import org.stepik.android.adaptive.ui.helper.CardHelper
import org.stepik.android.adaptive.ui.view.SwipeableLayout
import org.stepik.android.adaptive.util.HtmlUtil
import android.view.ViewGroup
import android.webkit.WebSettings
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.ui.DefaultWebViewClient
import org.stepik.android.adaptive.ui.adapter.attempts.AttemptAnswerAdapter
import org.stepik.android.adaptive.ui.view.container.ContainerView
import org.stepik.android.adaptive.util.changeVisibillity
import javax.inject.Inject

class QuizCardViewHolder(val binding: QuizCardViewBinding) : ContainerView.ViewHolder(binding.root), CardView {
    @Inject
    lateinit var screenManager: ScreenManager

    init {
        App.component().inject(this)

        val settings = binding.question.settings
        settings.allowContentAccess = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.javaScriptEnabled = true

        binding.question.webViewClient = DefaultWebViewClient(null) { _, _ -> onCardLoaded() }
        binding.question.setOnWebViewClickListener { path -> screenManager.showImage(binding.root.context, path) }
        binding.question.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        binding.next.setOnClickListener { binding.container.swipeDown() }
        binding.submit.setOnClickListener { presenter?.createSubmission() }
        binding.wrongRetry.setOnClickListener {
            presenter?.let {
                it.retrySubmission()
                CardHelper.resetSupplementalActions(binding)
            }
        }

        binding.bookmark.setOnClickListener {
            presenter?.toggleBookmark()
        }
        binding.container.setNestedScroll(binding.scroll)
    }

    private var hasSubmission = false
    private var presenter: CardPresenter? = null

    fun bind(presenter: CardPresenter) {
        this.presenter = presenter
        presenter.attachView(this)
    }

    fun onTopCard() {
        if (!hasSubmission) {
            if (presenter?.isLoading == true) {
                onSubmissionLoading()
            } else {
                binding.submit.visibility = View.VISIBLE
                (binding.answers.adapter as AttemptAnswerAdapter).isEnabled = true
            }
        }

        binding.container.setSwipeListener(object : SwipeableLayout.SwipeListener() {
            override fun onScroll(scrollProgress: Float) {
                binding.hardReaction.alpha = Math.max(2 * scrollProgress, 0f)
                binding.easyReaction.alpha = Math.max(2 * -scrollProgress, 0f)
            }

            override fun onSwipeLeft() {
                binding.easyReaction.alpha = 1f
                presenter?.createReaction(RecommendationReaction.Reaction.NEVER_AGAIN)
            }

            override fun onSwipeRight() {
                binding.hardReaction.alpha = 1f
                presenter?.createReaction(RecommendationReaction.Reaction.MAYBE_LATER)
            }
        })
    }

    private fun onCardLoaded() {
        binding.curtain.visibility = View.GONE
        if (presenter?.isLoading != true) binding.answersProgress.visibility = View.GONE
    }

    override fun setBookmarkState(isBookmarked: Boolean) {
        binding.bookmark.changeVisibillity(true)
        binding.bookmark.setImageResource(if (isBookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark
        })
    }

    override fun setTitle(title: String) {
        binding.title.text = title
    }

    override fun setQuestion(html: String) {
        HtmlUtil.setCardWebViewHtml(binding.question, html)
    }

    override fun setAnswerAdapter(adapter: AttemptAnswerAdapter<*>) {
        binding.answers.adapter = adapter
        adapter.submitButton = binding.submit
        adapter.isEnabled = false

        binding.submit.visibility = View.GONE
    }

    override fun setSubmission(submission: Submission, animate: Boolean) {
        CardHelper.resetSupplementalActions(binding)
        when (submission.status) {
            Submission.Status.CORRECT -> {
                binding.submit.visibility = View.GONE
                hasSubmission = true

                binding.correct.visibility = View.VISIBLE
                binding.next.visibility = View.VISIBLE
                binding.container.isEnabled = true

                if (submission.hint.isNotBlank()) {
                    binding.hint.text = submission.hint
                    binding.hint.visibility = View.VISIBLE
                }

                if (animate) {
                    CardHelper.scrollDown(binding.scroll)
                }
            }

            Submission.Status.WRONG -> {
                binding.wrong.visibility = View.VISIBLE
                hasSubmission = true

                binding.wrongRetry.visibility = View.VISIBLE
                binding.submit.visibility = View.GONE

                binding.container.isEnabled = true

                if (animate) {
                    CardAnimations.playWiggleAnimation(binding.container)
                }
            }
        }
    }

    override fun onSubmissionConnectivityError() =
            onSubmissionError(R.string.connectivity_error)


    override fun onSubmissionRequestError() =
            onSubmissionError(R.string.request_error)


    private fun onSubmissionError(@StringRes errorMessage: Int) {
        if (binding.root.parent != null) {
            Snackbar.make(binding.root.parent as ViewGroup, errorMessage, Snackbar.LENGTH_SHORT).show()
        }
        binding.container.isEnabled = true
        CardHelper.resetSupplementalActions(binding)
    }

    override fun onSubmissionLoading() {
        CardHelper.resetSupplementalActions(binding)
        binding.container.isEnabled = false
        binding.submit.visibility = View.GONE
        binding.answersProgress.visibility = View.VISIBLE

        CardHelper.scrollDown(binding.scroll)
    }
}