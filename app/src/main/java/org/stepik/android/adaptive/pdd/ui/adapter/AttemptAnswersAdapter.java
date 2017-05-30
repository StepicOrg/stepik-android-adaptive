package org.stepik.android.adaptive.pdd.ui.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.data.model.Attempt;
import org.stepik.android.adaptive.pdd.data.model.Reply;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.databinding.ItemAnswerBinding;

import java.util.List;


public class AttemptAnswersAdapter extends RecyclerView.Adapter<AttemptAnswersAdapter.AttemptAnswerViewHolder> {
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
        this.notifyDataSetChanged();
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
                notifyItemChanged(lastSelection);
                selectedCount--;
            }
            selection[pos] = true;
            selectedCount++;
        }
        lastSelection = pos;
        notifyItemChanged(pos);
        refreshSubmitButton();
    }

    public Submission getSubmission() {
        return new Submission(new Reply(selection), attempt.getId());
    }

    @Override
    public AttemptAnswerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new AttemptAnswerViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_answer, parent, false));
    }

    @Override
    public void onBindViewHolder(final AttemptAnswerViewHolder holder, int position) {
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
                    context.getResources().getColor(selection[position] ? R.color.colorAccent : R.color.colorRadioButtonDefault));
            holder.binding.itemAnswerSelectionImage.setImageDrawable(selectionImageDrawable);

            holder.binding.getRoot().setOnClickListener((v) -> this.select(holder.getAdapterPosition()));
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


    class AttemptAnswerViewHolder extends RecyclerView.ViewHolder {
        private final ItemAnswerBinding binding;

        AttemptAnswerViewHolder(final ItemAnswerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
