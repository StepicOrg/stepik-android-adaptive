package org.stepik.android.adaptive.ui.adapter.attempts

import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.Attempt
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.ui.view.container.ContainerView
import org.stepik.android.adaptive.util.changeVisibillity

class NotSupportedQuizAnswerAdapter: AttemptAnswerAdapter<ContainerView.ViewHolder>() {
    override fun getItemCount() = 1
    override fun createSubmission(): Submission? = null
    override fun onCreateViewHolder(parent: ViewGroup): ContainerView.ViewHolder =
        ContainerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.quiz_type_not_supported, parent, false))

    override fun setAttempt(attempt: Attempt?) {}
    override fun refreshSubmitButton() {
        submitButton?.changeVisibillity(false)
    }
}