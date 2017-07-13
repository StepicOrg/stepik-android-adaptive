package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.ui.view.container.ContainerAdapter;
import org.stepik.android.adaptive.pdd.ui.view.container.ContainerView;

import java.util.ArrayList;
import java.util.List;

public class QuizCardsContainer extends FrameLayout implements ContainerView {
    private final static int BUFFER_SIZE = 4;
    private final static int CARD_OFFSET = (int)(Resources.getSystem().getDisplayMetrics().density * 10);

    public QuizCardsContainer(@NonNull Context context) {
        super(context);
    }

    public QuizCardsContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QuizCardsContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final QuizCardView.QuizCardFlingListener quizCardFlingListener = new QuizCardView.QuizCardFlingListener() {
        @Override
        public void onScroll(float scrollProgress) {
            final int size = Math.min(adapter.getItemCount(), BUFFER_SIZE);
            float mul = Math.min(Math.abs(scrollProgress), 0.5f) * 2;
            for (int j = 1; j < size; j++) {
                setViewState(cardHolders.get(j).getView(), j - mul, false);
            }
        }

        @Override
        public void onSwipeLeft() {
            cardHolders.get(0).getView().setEnabled(false);
        }

        @Override
        public void onSwipeRight() {
            cardHolders.get(0).getView().setEnabled(false);
        }

        @Override
        public void onSwiped() {
            poll();
        }
    };

    private List<ContainerView.ViewHolder> cardHolders = new ArrayList<>();

    @org.jetbrains.annotations.Nullable
    public View getTopCardView() {
        ContainerView.ViewHolder holder = cardHolders.get(0);
        return holder.isAttached() ? holder.getView() : null;
    }

    private void initCards() {
        cardHolders.clear();
        for (int i = 0; i < BUFFER_SIZE; i++) {
            cardHolders.add(adapter.onCreateViewHolder(this));
        }
        onDataSetChanged();
    }

    private void poll() {
        removeView(cardHolders.remove(0).getView());
        adapter.poll();
        cardHolders.add(adapter.onCreateViewHolder(this));
        onRebind();
    }

    private void setViewState(View view, float mul, boolean allowEnable) {
        view.setScaleX(1 - (0.02f * mul));
        view.setScaleY(1 - (0.02f * mul));

        view.setEnabled(mul == 0 && allowEnable);
        view.setTranslationY(CARD_OFFSET * mul);

        if (mul >= BUFFER_SIZE - 2) {
            view.setAlpha(BUFFER_SIZE - mul - 1);
        }
    }

    private CardsAdapter adapter;

    @Override
    public final void setAdapter(@NotNull ContainerAdapter adapter) {
        if (adapter instanceof CardsAdapter) {
            this.adapter = (CardsAdapter) adapter;
            this.adapter.setContainer(this);
            initCards();
        }
    }

    @Override
    public void onDataSetChanged() {
        if (adapter == null) return;
        removeAllViews();
        onRebind();
    }

    @Override
    public void onDataAdded() {
        onRebind();
    }

    @Override
    public void onRebind() {
        final int size = Math.min(adapter.getItemCount(), BUFFER_SIZE);
        for (int i = 0; i < size; i++) {
            onRebind(i);
        }

        if (Util.isLowAndroidVersion()) {
            for (int i = size - 1; i >= 0; i--)
                cardHolders.get(i).getView().bringToFront();
        }
    }

    @Override
    public void onRebind(int i) {
        final int size = Math.min(adapter.getItemCount(), BUFFER_SIZE);
        if (0 <= i && i < cardHolders.size()) {
            ContainerView.ViewHolder holder = cardHolders.get(i);
            View view = holder.getView();
            if (!holder.isAttached()) {
                adapter.onBindViewHolder(holder, i);
                holder.setAttached(true);
                addView(view);
            }

            if (!Util.isLowAndroidVersion()) {
                view.setElevation(size + 3 - i);
            }

            setViewState(view, i, true);
            if (i == 0) {
                if (view instanceof QuizCardView) {
                    ((QuizCardView) view).setQuizCardFlingListener(quizCardFlingListener);
                }
                adapter.onBindTopCard(holder, 0);
            }
        }
    }

    public static abstract class CardsAdapter<VH extends ContainerView.ViewHolder> extends ContainerAdapter<VH> {
        protected abstract void poll();
        protected abstract void onBindTopCard(VH holder, int pos);
    }
}
