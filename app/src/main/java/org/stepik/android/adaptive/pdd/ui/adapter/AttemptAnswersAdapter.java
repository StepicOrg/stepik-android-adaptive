package org.stepik.android.adaptive.pdd.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.data.model.Attempt;
import org.stepik.android.adaptive.pdd.data.model.Reply;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.databinding.ItemAnswerBinding;
import org.stepik.android.adaptive.pdd.ui.view.container.ContainerAdapter;
import org.stepik.android.adaptive.pdd.ui.view.container.ContainerView;

import java.util.List;

public class AttemptAnswersAdapter extends ContainerAdapter<AttemptAnswersAdapter.AttemptAnswerViewHolder> {
    private Attempt attempt;
    private List<String> options;
    private boolean[] selection;
    private int lastSelection = -1;
    private int selectedCount = 0;

    private boolean enabled = true;

    private Button submitButton;

    public void setAttempt(final Attempt attempt) {
        this.attempt = attempt;
        if (attempt != null && attempt.getDataset() != null && attempt.getDataset().getOptions() != null) {
            this.options = attempt.getDataset().getOptions();
            this.selection = new boolean[this.options.size()];
        } else {
            this.options = null;
            this.selection = null;
        }
        this.lastSelection = -1;
        this.selectedCount = 0;
        this.onDataSetChanged();
    }

    public void setSubmitButton(final Button submitButton) {
        this.submitButton = submitButton;
        refreshSubmitButton();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void refreshSubmitButton() {
        if (submitButton != null) {
            submitButton.setEnabled(selectedCount > 0);
        }
    }

    private void select(final int pos) {
        if (!enabled) return;
        if (attempt.getDataset().is_multiple_choice()) {
            selectedCount += selection[pos] ? -1 : 1;
            selection[pos] = !selection[pos];
        } else {
            if (lastSelection != -1) {
                selection[lastSelection] = false;
                onRebind(lastSelection);
                selectedCount--;
            }
            selection[pos] = true;
            selectedCount++;
        }
        lastSelection = pos;
        onRebind(pos);
        refreshSubmitButton();
    }

    public Submission getSubmission() {
        return new Submission(new Reply(selection), attempt.getId());
    }

    @NotNull
    @Override
    public AttemptAnswerViewHolder onCreateViewHolder(@NotNull final ViewGroup parent) {
        return new AttemptAnswerViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_answer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull final AttemptAnswerViewHolder holder, int position) {
        if (options != null) {
            holder.binding.itemAnswerText.setText(options.get(position));

            final Context context = holder.binding.getRoot().getContext();

            int selectionImageDrawableId;
            if (attempt.getDataset().is_multiple_choice()) {
                selectionImageDrawableId = selection[position] ? R.drawable.ic_check_box_filled : R.drawable.ic_check_box;
            } else {
                selectionImageDrawableId = selection[position] ? R.drawable.ic_radio_button_filled : R.drawable.ic_radio_button;
            }

            final Drawable selectionImageDrawable =
                    DrawableCompat.wrap(ContextCompat.getDrawable(context, selectionImageDrawableId));
            DrawableCompat.setTint(selectionImageDrawable,
                    ContextCompat.getColor(context, selection[position] ? R.color.colorAccent : R.color.colorRadioButtonDefault));
            holder.binding.itemAnswerSelectionImage.setImageDrawable(selectionImageDrawable);

            holder.binding.getRoot().setOnClickListener((v) -> this.select(position));
        }
    }

    @Override
    public int getItemCount() {
        if (options != null) {
            return options.size();
        }
        return 0;
    }

    public void clear() {
        setAttempt(null);
    }


    class AttemptAnswerViewHolder extends ContainerView.ViewHolder {
        private final ItemAnswerBinding binding;

        AttemptAnswerViewHolder(final ItemAnswerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
