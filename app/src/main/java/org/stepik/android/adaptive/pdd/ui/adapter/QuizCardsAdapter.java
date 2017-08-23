package org.stepik.android.adaptive.pdd.ui.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.core.presenter.CardPresenter;
import org.stepik.android.adaptive.pdd.data.model.Card;
import org.stepik.android.adaptive.pdd.ui.listener.AdaptiveReactionListener;
import org.stepik.android.adaptive.pdd.ui.listener.AnswerListener;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardsContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizCardsAdapter extends QuizCardsContainer.CardsAdapter<QuizCardViewHolder> {
    private List<CardPresenter> presenters = new ArrayList<>();
    private final AdaptiveReactionListener listener;
    private final AnswerListener answerListener;

    public QuizCardsAdapter(AdaptiveReactionListener listener, AnswerListener answerListener) {
        this.listener = listener;
        this.answerListener = answerListener;
    }

    public void recycle() {
        for (final CardPresenter presenter : presenters) {
            presenter.destroy();
        }
    }

    /**
     * Method that being called onDestroyView to properly detach view from presenters
     */
    public void detach() {
        for (final CardPresenter presenter : presenters) {
            presenter.detachView();
        }
    }

    public boolean isCardExists(long lessonId) {
        for (CardPresenter presenter : presenters) {
            if (presenter.getCard().getLessonId() == lessonId) return true;
        }
        return false;
    }

    @NotNull
    @Override
    public QuizCardViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        return new QuizCardViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.quiz_card_view, parent, false));
    }

    @Override
    public int getItemCount() {
        return presenters.size();
    }

    @Override
    public void onBindViewHolder(@NotNull QuizCardViewHolder holder, int pos) {
        holder.bind(presenters.get(pos));
    }

    @Override
    protected void onBindTopCard(QuizCardViewHolder holder, int pos) {
        holder.onTopCard();
    }

    @Override
    protected void onPositionChanged(QuizCardViewHolder holder, int pos) {
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) holder.getBinding().card.getLayoutParams();
        if (pos > 1) {
            p.height = QuizCardsContainer.CARD_OFFSET * 2;
            changeVisibilityOfAllChildrenTo(holder.getBinding().card, View.GONE, Collections.singletonList(R.id.curtain));
        } else {
            p.height = FrameLayout.LayoutParams.MATCH_PARENT;
            changeVisibilityOfAllChildrenTo(holder.getBinding().card, View.VISIBLE, Collections.singletonList(R.id.curtain));
        }
        holder.getBinding().card.setLayoutParams(p);
    }

    private static void changeVisibilityOfAllChildrenTo(ViewGroup viewGroup, int visibility, List<Integer> exclude) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; ++i) {
            final View view = viewGroup.getChildAt(i);
            if (exclude != null && exclude.contains(view.getId())) continue;
            view.setVisibility(visibility);
        }
    }

    public void add(Card card) {
        presenters.add(new CardPresenter(card, listener, answerListener));
        onDataAdded();
    }

    public boolean isEmptyOrContainsOnlySwipedCard(final long lesson) {
        return presenters.isEmpty() || (presenters.size() == 1 && presenters.get(0).getCard().getLessonId() == lesson);
    }

    @Override
    protected void poll() {
        presenters.remove(0).destroy();
    }
}
