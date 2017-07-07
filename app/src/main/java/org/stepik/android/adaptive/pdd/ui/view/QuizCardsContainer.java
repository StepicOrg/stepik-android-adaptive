package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.adaptive.pdd.Util;

import java.util.ArrayList;
import java.util.List;

public class QuizCardsContainer extends FrameLayout {
    private final static int BUFFER_SIZE = 3;
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

    private CardsAdapter adapter;

    public final void setAdapter(@NotNull CardsAdapter adapter) {
        this.adapter = adapter;
        this.adapter.container = this;
        initCards();
    }


    private List<CardViewHolder> cardHolders = new ArrayList<>();

    @org.jetbrains.annotations.Nullable
    public View getTopCardView() {
        CardViewHolder holder = cardHolders.get(0);
        return holder.isAttached() ? holder.getView() : null;
    }

    private void initCards() {
        cardHolders.clear();
        for (int i = 0; i < BUFFER_SIZE; i++) {
            cardHolders.add(adapter.onCreateViewHolder(this));
        }
        notifyDataSetChanged();
    }


    public void notifyDataSetChanged() {
        if (adapter == null) return;
        removeAllViews();
        bindHolders();
    }

    private void bindHolders() {
        final int size = Math.min(adapter.getItemCount(), BUFFER_SIZE);
        for (int i = 0; i < size; i++) {
            CardViewHolder holder = cardHolders.get(i);
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

        if (Util.isLowAndroidVersion()) {
            for (int i = size - 1; i >= 0; i--)
                cardHolders.get(i).getView().bringToFront();
        }
    }

    private void poll() {
        removeView(cardHolders.remove(0).getView());
        adapter.poll();
        cardHolders.add(adapter.onCreateViewHolder(this));
        bindHolders();
    }

    private void setViewState(View view, float mul, boolean allowEnable) {
        view.setScaleX(1 - (0.02f * mul));
        view.setScaleY(1 - (0.02f * mul));

        view.setEnabled(mul == 0 && allowEnable);
        view.setTranslationY(CARD_OFFSET * mul);
    }

    public static abstract class CardsAdapter<VH extends CardViewHolder> {
        private QuizCardsContainer container;

        protected final void notifyDataAdded() {
            if (container != null) container.bindHolders();
        }

        protected abstract VH onCreateViewHolder(ViewGroup parent);
        protected abstract int getItemCount();
        protected abstract void onBindViewHolder(VH holder, int pos);
        protected abstract void poll();
        protected abstract void onBindTopCard(VH holder, int pos);
    }

    public static abstract class CardViewHolder {
        private View view;
        private boolean attached = false;

        protected CardViewHolder(View view) {
            this.view = view;
        }

        public View getView() {
            return view;
        }

        private boolean isAttached() {
            return attached;
        }

        private void setAttached(boolean attached) {
            this.attached = attached;
        }
    }
}
