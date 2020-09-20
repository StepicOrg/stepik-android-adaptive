package org.stepik.android.adaptive.ui.adapter.attempts

import android.text.InputType
import org.stepik.android.adaptive.data.model.Reply
import org.stepik.android.adaptive.data.model.Submission

class NumberQuizAnswerAdapter : SingleLineAnswersAdapter() {
    override fun onBindViewHolder(holder: StringViewHolder, pos: Int) {
        super.onBindViewHolder(holder, pos)
        holder.editField.inputType = InputType.TYPE_CLASS_NUMBER
    }

    override fun createSubmission(): Submission? =
        attemptId?.let {
            Submission(Reply(number = value), it)
        }
}
