package org.stepik.android.adaptive.pdd.core.presenter.contracts

import org.stepik.android.adaptive.pdd.data.model.Submission
import org.stepik.android.adaptive.pdd.ui.adapter.AttemptAnswersAdapter

interface CardView {
    fun setSubmission(submission: Submission, animate: Boolean)
    fun onSubmissionConnectivityError()
    fun onSubmissionRequestError()
    fun onSubmissionLoading()

    fun setTitle(title: String)
    fun setQuestion(html: String)
    fun setAnswerAdapter(adapter: AttemptAnswersAdapter)
}