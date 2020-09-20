package org.stepik.android.adaptive.ui.adapter.attempts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.Attempt
import org.stepik.android.adaptive.data.model.Reply
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.databinding.ItemAnswerBinding
import org.stepik.android.adaptive.ui.view.container.ContainerView

class ChoiceQuizAnswerAdapter : AttemptAnswerAdapter<ChoiceQuizAnswerAdapter.ChoiceQuizViewHolder>() {
    private var state: AdapterState? = null

    private var lastSelection = -1
    private var selectedCount = 0

    val lastSelectedAnswerText: String?
        get() = state?.options?.getOrNull(lastSelection)

    override fun setAttempt(attempt: Attempt?) {
        state = attempt?.getDataset()?.options?.let { options ->
            AdapterState(attempt, options, BooleanArray(options.size))
        }

        lastSelection = -1
        selectedCount = 0
        onDataSetChanged()
    }

    override fun createSubmission(): Submission? =
        state?.let { (attempt, _, selection) ->
            Submission(Reply(choices = selection), attempt.id)
        }

    override fun refreshSubmitButton() {
        submitButton?.isEnabled = selectedCount > 0
    }

    private fun select(pos: Int) {
        if (!isEnabled) return
        state?.let { (attempt, _, selection) ->
            if (attempt.getDataset()?.is_multiple_choice == true) {
                selectedCount += if (selection[pos]) -1 else 1
                selection[pos] = !selection[pos]
            } else {
                if (lastSelection != -1) {
                    selection[lastSelection] = false
                    onRebind(lastSelection)
                    selectedCount--
                }
                selection[pos] = true
                selectedCount++
            }
            lastSelection = pos
            onRebind(pos)
            refreshSubmitButton()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): ChoiceQuizViewHolder =
        ChoiceQuizViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_answer, parent, false))

    override fun onBindViewHolder(holder: ChoiceQuizViewHolder, pos: Int) {
        state?.let { (attempt, options, selection) ->
            holder.binding.itemAnswerText.text = options[pos]

            val context = holder.binding.root.context

            @DrawableRes
            val selectionImageDrawableId = if (attempt.getDataset()?.is_multiple_choice == true) {
                if (selection[pos]) R.drawable.ic_check_box_filled else R.drawable.ic_check_box
            } else {
                if (selection[pos]) R.drawable.ic_radio_button_filled else R.drawable.ic_radio_button
            }

            val selectionImageDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, selectionImageDrawableId)!!)
            DrawableCompat.setTint(
                selectionImageDrawable,
                ContextCompat.getColor(context, if (selection[pos]) R.color.colorAccent else R.color.colorRadioButtonDefault)
            )
            holder.binding.itemAnswerSelectionImage.setImageDrawable(selectionImageDrawable)

            holder.binding.root.setOnClickListener { select(pos) }
        }
    }

    override fun getItemCount() =
        state?.options?.size ?: 0

    class ChoiceQuizViewHolder(val binding: ItemAnswerBinding) : ContainerView.ViewHolder(binding.root)

    private data class AdapterState(
        val attempt: Attempt,
        val options: List<String>,
        val selection: BooleanArray
    )
}
