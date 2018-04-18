package org.stepik.android.adaptive.ui.adapter.attempts

import android.widget.Button
import org.stepik.android.adaptive.data.model.Attempt
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.ui.view.container.ContainerAdapter
import org.stepik.android.adaptive.ui.view.container.ContainerView

abstract class AttemptAnswerAdapter<VH : ContainerView.ViewHolder>: ContainerAdapter<VH>() {
    var isEnabled = true
        set(value) {
            field = value
            onRebind()
        }

    var submitButton: Button? = null
        set(value) {
            field = value
            refreshSubmitButton()
        }

    abstract fun setAttempt(attempt: Attempt?)
    abstract fun createSubmission(): Submission?

    override fun onBindViewHolder(holder: VH, pos: Int) {}

    fun clear() {
        setAttempt(null)
    }

    protected abstract fun refreshSubmitButton()
}