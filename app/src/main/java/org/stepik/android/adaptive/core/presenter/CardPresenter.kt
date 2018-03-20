package org.stepik.android.adaptive.core.presenter

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.api.API
import org.stepik.android.adaptive.api.SubmissionResponse
import org.stepik.android.adaptive.core.presenter.contracts.CardView
import org.stepik.android.adaptive.data.AnalyticMgr
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.data.model.*
import org.stepik.android.adaptive.ui.listener.AdaptiveReactionListener
import org.stepik.android.adaptive.ui.listener.AnswerListener
import org.stepik.android.adaptive.util.HtmlUtil
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


class CardPresenter(val card: Card, private val listener: AdaptiveReactionListener?, private val answerListener: AnswerListener?) : PresenterBase<CardView>() {
    private var submission: Submission? = null
    private var error: Throwable? = null

    private var disposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()

    private var isBookmarked: Boolean? = null

    var isLoading = false
        private set

    override fun attachView(view: CardView) {
        super.attachView(view)
        view.setTitle(card.lesson.title)
        view.setQuestion(HtmlUtil.prepareCardHtml(card.step.block.text))
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
        compositeDisposable.add(Single.fromCallable {
            DataBaseMgr.instance.isInBookmarks(card.step.id)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            isBookmarked = it
            resolveBookmarkState()
        }, {}))

    private fun resolveBookmarkState() {
        if (card.lessonId != Card.MOCK_LESSON_ID) { // do not show bookmark button for mock cards
            isBookmarked?.let { isBookmarked ->
                view?.setBookmarkState(isBookmarked)
            }
        }
    }

    private fun createBookmark(): Bookmark {
        val definition = if (card.isCorrect) {
            card.adapter.lastSelectedAnswerText ?: String()
        } else {
            String()
        }

        return Bookmark(
                QuestionsPack.values()[SharedPreferenceMgr.getInstance().questionsPackIndex].courseId,
                card.step.id,
                card.lesson.title,
                definition
        )
    }

    fun toggleBookmark() = isBookmarked?.let { bookmarked ->
        isBookmarked = null
        val bookmark = createBookmark()

        compositeDisposable.add(Single.fromCallable {
            if (bookmarked) {
                AnalyticMgr.getInstance().logEvent(AnalyticMgr.EVENT_ON_BOOKMARK_REMOVED)
                DataBaseMgr.instance.removeBookmark(bookmark)
            } else {
                AnalyticMgr.getInstance().logEvent(AnalyticMgr.EVENT_ON_BOOKMARK_ADDED)
                DataBaseMgr.instance.addBookmark(bookmark)
            }
            !bookmarked
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            isBookmarked = it
            resolveBookmarkState()
        }, {}))
    }

    /**
     * Add definition to bookmark if it was added before
     */
    private fun updateBookmark() {
        val bookmark = createBookmark()
        compositeDisposable.add(Completable.fromCallable {
            DataBaseMgr.instance.updateBookmark(bookmark)
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe())
    }

    fun createReaction(reaction: RecommendationReaction.Reaction) {
        val lesson = card.lessonId
        when(reaction) {
            RecommendationReaction.Reaction.NEVER_AGAIN -> {
                if (card.isCorrect) {
                    AnalyticMgr.getInstance().reactionEasyAfterCorrect(lesson)
                }
                AnalyticMgr.getInstance().reactionEasy(lesson)
            }

            RecommendationReaction.Reaction.MAYBE_LATER -> {
                if (card.isCorrect) {
                    AnalyticMgr.getInstance().reactionHardAfterCorrect(lesson)
                }
                AnalyticMgr.getInstance().reactionHard(lesson)
            }
        }
        listener?.createReaction(lesson, reaction)
    }

    fun createSubmission() {
        if (disposable == null || disposable?.isDisposed != false) {
            card.adapter.setEnabled(false)
            view?.onSubmissionLoading()
            isLoading = true
            error = null

            val submission = card.adapter.submission
            disposable = API.getInstance().createSubmission(submission)
                    .andThen(API.getInstance().getSubmissions(submission.attempt))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onSubmissionLoaded, this::onError)

            AnalyticMgr.getInstance().onSubmissionWasMade()
        }
    }

    fun retrySubmission() {
        submission = null
        card.adapter.setEnabled(true)
    }

    private fun onSubmissionLoaded(submissionResponse: SubmissionResponse) {
        submission = submissionResponse.firstSubmission
        submission?.let {
            if (it.status == Submission.Status.EVALUATION) {
                disposable =  API.getInstance().getSubmissions(it.attempt)
                        .delay(1, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onSubmissionLoaded, this::onError)
            } else {
                isLoading = false
                AnalyticMgr.getInstance().answerResult(card.step, it)
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
        card.adapter.setEnabled(true)
        if (error is HttpException) {
            view?.onSubmissionRequestError()
        } else {
            view?.onSubmissionConnectivityError()
        }
    }
}