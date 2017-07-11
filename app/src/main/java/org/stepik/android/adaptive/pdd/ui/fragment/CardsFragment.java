package org.stepik.android.adaptive.pdd.ui.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.github.jinatonic.confetti.CommonConfetti;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.data.model.Card;
import org.stepik.android.adaptive.pdd.data.model.Recommendation;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.adapter.QuizCardsAdapter;
import org.stepik.android.adaptive.pdd.ui.dialog.ExpLevelDialog;
import org.stepik.android.adaptive.pdd.ui.helper.CardHelper;
import org.stepik.android.adaptive.pdd.ui.listener.AnswerListener;
import org.stepik.android.adaptive.pdd.util.ExpUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public final class CardsFragment extends Fragment implements AnswerListener {
    private final static String TAG = "CardsFragment";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final PublishSubject<View> retrySubject = PublishSubject.create();

    private final Queue<Card> cards = new ArrayDeque<>();

    private String[] loadingPlaceholders;

    private FragmentRecommendationsBinding binding;

    private Disposable cardDisposable;

    private boolean isError = false;
    private boolean isCourseCompleted = false;

    private final QuizCardsAdapter adapter = new QuizCardsAdapter(this::createReaction, this);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        compositeDisposable.add(retrySubject.observeOn(AndroidSchedulers.mainThread()).subscribe(v -> retry()));
        loadingPlaceholders = getResources().getStringArray(R.array.recommendation_loading_placeholders);
        adapter.attachFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommendations, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.fragmentRecommendationsToolbar);

        if (savedInstanceState == null) {
            createReaction(0, RecommendationReaction.Reaction.INTERESTING);
        }

        binding.fragmentRecommendationsTryAgain.setOnClickListener(retrySubject::onNext);
        binding.fragmentRecommendationsCourseCompletedText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.fragmentRecommendationsLoadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);

        binding.fragmentRecommendationsProgress.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        binding.fragmentRecommendationsCardsContainer.setAdapter(adapter);

        if (isCourseCompleted) {
            courseCompleted();
        } else {
            resubscribe();
            if (isError) {
                onError(null);
            }
        }

        updateExpProgressBar(ExpUtil.getExp(), 0, false);
        return binding.getRoot();
    }

    public void createReaction(final long lesson, final RecommendationReaction.Reaction reaction) {
        if (adapter.getItemCount() == 0) {
            binding.fragmentRecommendationsProgress.setVisibility(View.VISIBLE);
            binding.fragmentRecommendationsLoadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);
        }

        compositeDisposable.add(CardHelper.createReactionObservable(lesson, reaction, cards.size())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::onError)
                .retryWhen(x -> x.zipWith(retrySubject, (a, b) -> a))
                .subscribe(this::onRecommendation, this::onError));
    }

    private void onRecommendation(final RecommendationsResponse response) {
        final List<Recommendation> recommendations = response.getRecommendations();
        if (recommendations == null || recommendations.isEmpty()) {
            courseCompleted();
        } else {
            int size = cards.size();
            for (final Recommendation recommendation : recommendations) {
                if (!isCardExists(recommendation.getLesson())) {
                    cards.add(new Card(recommendation.getLesson()));
                }
            }
            if (binding != null && size == 0) resubscribe();
        }
    }

    private void updateExpProgressBar(final long exp, final long streak, final boolean showLevelDialog) {
        final long level = ExpUtil.getCurrentLevel(exp);

        final long prev = ExpUtil.getNextLevelExp(level - 1);
        final long next = ExpUtil.getNextLevelExp(level);
        binding.fragmentRecommendationsExpProgress.setMax((int) (next - prev));
        binding.fragmentRecommendationsExpProgress.setProgress((int) (exp - prev));

        binding.fragmentRecommendationsExpCounter.setText(Long.toString(exp)); //String.format(getString(R.string.exp_current_progress), exp - prev, next - prev));
        binding.fragmentRecommendationsExpLevel.setText(String.format(getString(R.string.exp_title), level));
        binding.fragmentRecommendationsExpLevelNext.setText(String.format(getString(R.string.exp_subtitle), next - exp));

        if (showLevelDialog && level != ExpUtil.getCurrentLevel(exp - streak)) {
            onLevelGained(level);
        }
    }

    public void onCorrectAnswer() {
        final long streak = ExpUtil.incStreak();
        binding.fragmentRecommendationsExpInc.setText(String.format(getString(R.string.exp_inc), streak));
        binding.fragmentRecommendationsExpInc.setAlpha(1);
        binding.fragmentRecommendationsExpInc.animate()
                .alpha(0)
                .setInterpolator(new DecelerateInterpolator())
                .setStartDelay(1500)
                .setDuration(200)
                .start();

        int[] pos = new int[2];
        binding.fragmentRecommendationsExpInc.getLocationInWindow(pos);

        CommonConfetti.explosion((CoordinatorLayout) binding.getRoot(), pos[0] + binding.fragmentRecommendationsExpInc.getWidth() / 2, binding.fragmentRecommendationsToolbar.getHeight() / 2, new int[]{
                Color.BLACK,
                ContextCompat.getColor(getContext(), R.color.colorAccentDisabled),
                ContextCompat.getColor(getContext(), R.color.colorAccent)
        }).oneShot();
        updateExpProgressBar(ExpUtil.addExp(streak), streak, true);
    }

    public void onWrongAnswer() {
        ExpUtil.resetStreak();
    }

    private void onLevelGained(final long level) {
        ExpLevelDialog.Companion.newInstance(level).show(getChildFragmentManager(), "LEVEL");
    }

    /**
     * Resubscribes to card
     */
    private void resubscribe() {
        if (!cards.isEmpty()) {
            if (cardDisposable != null && !cardDisposable.isDisposed()) {
                cardDisposable.dispose();
            }
            cardDisposable = cards.peek()
                    .subscribe(this::onCardDataLoaded, this::onError);
        }
    }

    private void onError(final Throwable error) {
        isError = true;
        if (error != null) error.printStackTrace();
        if (binding != null) {
            binding.fragmentRecommendationsCardsContainer.setVisibility(View.GONE);
            binding.fragmentRecommendationsError.setVisibility(View.VISIBLE);
            binding.fragmentRecommendationsProgress.setVisibility(View.GONE);
        }
    }

    /**
     * Retry to load a top card
     */
    private void retry() {
        isError = false;
        binding.fragmentRecommendationsError.setVisibility(View.GONE);
        binding.fragmentRecommendationsProgress.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsCardsContainer.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsLoadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);
        if (!cards.isEmpty()) {
            cards.peek().init();
            resubscribe();
        }
    }

    /**
     * Set ui of card should be called only between onCreateView - onDestroyView
     * @param card - current card
     */
    private void onCardDataLoaded(final Card card) {
        adapter.add(card);
        binding.fragmentRecommendationsProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsCardsContainer.setVisibility(View.VISIBLE);
        cards.poll();
        resubscribe();
    }


    private void courseCompleted() {
        isCourseCompleted = true;
        if (binding != null) {
            binding.fragmentRecommendationsCardsContainer.setVisibility(View.GONE);
            binding.fragmentRecommendationsProgress.setVisibility(View.GONE);
            binding.fragmentRecommendationsCourseCompleted.setVisibility(View.VISIBLE);
        }
    }

    private boolean isCardExists(final long lessonId) {
        for (final Card card : cards) {
            if (card.getLessonId() == lessonId) return true;
        }
        return adapter.isCardExists(lessonId);
    }

    @Override
    public void onDestroyView() {
        adapter.detach();
        if (cardDisposable != null) cardDisposable.dispose();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        for (final Card card : cards) {
            card.recycle();
        }
        adapter.recycle();
        super.onDestroy();
    }
}
