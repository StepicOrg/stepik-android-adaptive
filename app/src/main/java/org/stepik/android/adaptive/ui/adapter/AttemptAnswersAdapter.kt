package org.stepik.android.adaptive.ui.adapter

import android.databinding.DataBindingUtil
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.model.Attempt
import org.stepik.android.adaptive.data.model.Reply
import org.stepik.android.adaptive.data.model.Submission
import org.stepik.android.adaptive.databinding.ItemAnswerBinding
import org.stepik.android.adaptive.ui.view.container.ContainerAdapter
import org.stepik.android.adaptive.ui.view.container.ContainerView

class AttemptAnswersAdapter : ContainerAdapter<AttemptAnswersAdapter.AttemptAnswerViewHolder>() {
    private var state: AdapterState? = null

    private var lastSelection = -1
    private var selectedCount = 0

    var isEnabled = true
    var submitButton: Button? = null
        set(value) {
            field = value
            refreshSubmitButton()
        }

    val lastSelectedAnswerText: String?
        get() = state?.options?.getOrNull(lastSelection)

    fun setAttempt(attempt: Attempt?) {
        state = attempt?.dataset?.options?.let { options ->
            AdapterState(attempt, options, BooleanArray(options.size))
        }

        lastSelection = -1
        selectedCount = 0
        onDataSetChanged()
    }

    fun createSubmission(): Submission? = state?.let { (attempt, _, selection) ->
        Submission(Reply(selection), attempt.id)
    }

    private fun refreshSubmitButton() {
        submitButton?.isEnabled = selectedCount > 0
    }

    private fun select(pos: Int) {
        if (!isEnabled) return
        state?.let { (attempt, _, selection) ->
            if (attempt.dataset.is_multiple_choice) {
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

    override fun onCreateViewHolder(parent: ViewGroup): AttemptAnswerViewHolder =
            AttemptAnswerViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_answer, parent, false))

    override fun onBindViewHolder(holder: AttemptAnswerViewHolder, pos: Int) {
        state?.let { (attempt, options, selection) ->
            holder.binding.itemAnswerText.text = options[pos]

            val context = holder.binding.root.context

            @DrawableRes
            val selectionImageDrawableId = if (attempt.dataset.is_multiple_choice) {
                if (selection[pos]) R.drawable.ic_check_box_filled else R.drawable.ic_check_box
            } else {
                if (selection[pos]) R.drawable.ic_radio_button_filled else R.drawable.ic_radio_button
            }

            val selectionImageDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, selectionImageDrawableId))
            DrawableCompat.setTint(selectionImageDrawable,
                    ContextCompat.getColor(context, if (selection[pos]) R.color.colorAccent else R.color.colorRadioButtonDefault))
            holder.binding.itemAnswerSelectionImage.setImageDrawable(selectionImageDrawable)

            holder.binding.root.setOnClickListener { select(pos) }
        }
    }

    override fun getItemCount() =
            state?.options?.size ?: 0

    fun clear() {
        setAttempt(null)
    }

    class AttemptAnswerViewHolder(val binding: ItemAnswerBinding) : ContainerView.ViewHolder(binding.root)

    private data class AdapterState(
            val attempt: Attempt,
            val options: List<String>,
            val selection: BooleanArray
    )
}
