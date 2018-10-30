package org.stepik.android.adaptive.core.presenter

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.SubmissionResponse
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.presenter.contracts.CardView
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.*
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.ui.adapter.attempts.ChoiceQuizAnswerAdapter
import org.stepik.android.adaptive.ui.listener.AdaptiveReactionListener
import org.stepik.android.adaptive.ui.listener.AnswerListener
import org.stepik.android.adaptive.util.HtmlUtil
import org.stepik.android.adaptive.util.addDisposable
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CardPresenter(val card: Card, private val listener: AdaptiveReactionListener?, private val answerListener: AnswerListener?) : PresenterBase<CardView>() {
    private var submission: Submission? = null
    private var error: Throwable? = null

    private var disposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()

    private var isBookmarked: Boolean? = null

    var isLoading = false
        private set

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var dataBaseMgr: DataBaseMgr

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var questionsPacksManager: QuestionsPacksManager

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    init {
        App.componentManager().studyComponent.inject(this)
    }

    override fun attachView(view: CardView) {
        super.attachView(view)
        view.setTitle(card.lesson.title)
        view.setQuestion(HtmlUtil.prepareCardHtml(card.step.block.text, config.host))
        view.setAnswerAdapter(card.adapter)

        fetchBookmarkState()

        if (isLoading) view.onSubmissionLoading()
        submission?.let { view.setSubmission(it, false) }
        error?.let(::onError)
    }

    fun detachView() {
        view?.let {
            super.detachView(it)
        }
    }

    override fun destroy() {
        card.recycle()
        disposable?.dispose()
    }

    private fun fetchBookmarkState() =
        compositeDisposable addDisposable dataBaseMgr.isInBookmarks(card.step.id)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    isBookmarked = it
                    resolveBookmarkState()
                }, {})

    private fun resolveBookmarkState() {
        if (card.lessonId != Card.MOCK_LESSON_ID && config.isBookmarksSupported) { // do not show bookmark button for mock cards and unsupported flavours
            isBookmarked?.let { isBookmarked ->
                view?.setBookmarkState(isBookmarked)
            }
        }
    }

    private fun createBookmark(): Bookmark {
        val definition = if (card.isCorrect) {
            (card.adapter as? ChoiceQuizAnswerAdapter)?.lastSelectedAnswerText ?: String()
        } else {
            String()
        }

        return Bookmark(
                questionsPacksManager.currentCourseId,
                card.step.id,
                card.lesson.title,
                definition
        )
    }

    fun toggleBookmark() = isBookmarked?.let { bookmarked ->
        isBookmarked = null
        val bookmark = createBookmark()

        compositeDisposable addDisposable
                if (bookmarked) {
                    analytics.logEvent(Analytics.EVENT_ON_BOOKMARK_REMOVED)
                    dataBaseMgr.removeBookmark(bookmark)
                } else {
                    analytics.logEvent(Analytics.EVENT_ON_BOOKMARK_ADDED)
                    dataBaseMgr.addBookmark(bookmark)
                }.andThen(Single.just(!bookmarked))
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe({
                            isBookmarked = it
                            resolveBookmarkState()
                        }, {})
    }

    /**
     * Add definition to bookmark if it was added before
     */
    private fun updateBookmark() {
        val bookmark = createBookmark()
        compositeDisposable addDisposable dataBaseMgr.updateBookmark(bookmark).subscribeOn(backgroundScheduler).observeOn(backgroundScheduler).subscribe()
    }

    fun createReaction(reaction: RecommendationReaction.Reaction) {
        val lesson = card.lessonId
        when(reaction) {
            RecommendationReaction.Reaction.NEVER_AGAIN -> {
                if (card.isCorrect) {
                    analytics.reactionEasyAfterCorrect(lesson)
                }
                analytics.reactionEasy(lesson)
            }

            RecommendationReaction.Reaction.MAYBE_LATER -> {
                if (card.isCorrect) {
                    analytics.reactionHardAfterCorrect(lesson)
                }
                analytics.reactionHard(lesson)
            }
            else -> {}
        }
        listener?.createReaction(lesson, reaction)
    }

    fun createSubmission() {
        if (disposable == null || disposable?.isDisposed != false) {
            card.adapter.createSubmission()?.let { submission ->
                card.adapter.isEnabled = false
                view?.onSubmissionLoading()
                isLoading = true
                error = null

                disposable = api.createSubmission(submission)
                        .andThen(api.getSubmissions(submission.attempt))
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(this::onSubmissionLoaded, this::onError)

                analytics.onSubmissionWasMade()
            }
        }
    }

    fun retrySubmission() {
        submission = null
        card.adapter.isEnabled  = true
    }

    private fun onSubmissionLoaded(submissionResponse: SubmissionResponse) {
        submission = submissionResponse.firstSubmission
        submission?.let {
            if (it.status == Submission.Status.EVALUATION) {
                disposable = api.getSubmissions(it.attempt)
                        .delay(1, TimeUnit.SECONDS)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(this::onSubmissionLoaded, this::onError)
            } else {
                isLoading = false
                analytics.answerResult(card.step, it)
                if (it.status == Submission.Status.CORRECT) {
                    listener?.createReaction(card.lessonId, RecommendationReaction.Reaction.SOLVED)
                    answerListener?.onCorrectAnswer(it.id)
                    card.onCorrect()
                    updateBookmark()
                }
                if (it.status == Submission.Status.WRONG) {
                    answerListener?.onWrongAnswer()
                }
                view?.setSubmission(it, true)
            }
        }
    }

    private fun onError(error: Throwable) {
        isLoading = false
        this.error = error
        card.adapter.isEnabled = true
        if (error is HttpException) {
            view?.onSubmissionRequestError()
        } else {
            view?.onSubmissionConnectivityError()
        }
    }
}