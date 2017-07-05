package org.stepik.android.adaptive.pdd.ui.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.core.ScreenManager;
import org.stepik.android.adaptive.pdd.core.presenter.CardPresenter;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.model.Card;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.databinding.QuizCardViewBinding;
import org.stepik.android.adaptive.pdd.ui.DefaultWebViewClient;
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment;
import org.stepik.android.adaptive.pdd.ui.helper.CardHelper;
import org.stepik.android.adaptive.pdd.ui.listener.AdaptiveReactionListener;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardView;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardsContainer;
import org.stepik.android.adaptive.pdd.util.HtmlUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class QuizCardsAdapter extends QuizCardsContainer.CardsAdapter<QuizCardViewHolder> {
    private List<CardPresenter> presenters = new ArrayList<>();
    private final AdaptiveReactionListener listener;

    public QuizCardsAdapter(AdaptiveReactionListener listener) {
        this.listener = listener;
    }

    private WeakReference<CardsFragment> fragmentWeakReference = new WeakReference<>(null);

    public void attachFragment(CardsFragment fragment) {
        fragmentWeakReference = new WeakReference<>(fragment);
    }

    public void recycle() {
        fragmentWeakReference.clear();
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

    @Override
    protected QuizCardViewHolder onCreateViewHolder(ViewGroup parent) {
        return new QuizCardViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.quiz_card_view, parent, false));
    }

    @Override
    public int getItemCount() {
        return presenters.size();
    }

    @Override
    protected void onBindViewHolder(QuizCardViewHolder holder, int pos) {
        holder.bind(presenters.get(pos));
    }

    @Override
    protected void onBindTopCard(QuizCardViewHolder holder, int pos) {
        holder.onTopCard();
    }

    public void add(Card card) {
        presenters.add(new CardPresenter(card, listener));
        notifyDataAdded();
    }

    @Override
    protected void poll() {
        presenters.remove(0).destroy();
    }
}
