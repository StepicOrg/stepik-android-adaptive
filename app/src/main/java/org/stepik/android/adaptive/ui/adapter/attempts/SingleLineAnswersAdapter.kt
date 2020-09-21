package org.stepik.android.adaptive.ui.adapter.attempts

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.Attempt
import org.stepik.android.adaptive.data.model.Reply
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.ui.view.container.ContainerView

open class SingleLineAnswersAdapter : AttemptAnswerAdapter<SingleLineAnswersAdapter.StringViewHolder>() {
    protected var value = String()
    protected var attemptId: Long? = null

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            value = s?.toString() ?: ""
            refreshSubmitButton()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun setAttempt(attempt: Attempt?) {
        attemptId = attempt?.id
    }

    override fun createSubmission(): Submission? =
        attemptId?.let {
            Submission(Reply(text = value), it)
        }

    override fun onBindViewHolder(holder: StringViewHolder, pos: Int) {
        holder.editField.setText(value)
        holder.editField.isEnabled = isEnabled
    }

    override fun refreshSubmitButton() {
        submitButton?.isEnabled = value.isNotBlank()
    }

    override fun onCreateViewHolder(parent: ViewGroup): StringViewHolder =
        StringViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.quiz_type_string, parent, false) as EditText).apply {
            editField.addTextChangedListener(textWatcher)
        }

    override fun getItemCount(): Int = 1

    class StringViewHolder(val editField: EditText) : ContainerView.ViewHolder(editField)
}
