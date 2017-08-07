package org.stepik.android.adaptive.pdd.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.db.DataBaseMgr;
import org.stepik.android.adaptive.pdd.data.model.Card;
import org.stepik.android.adaptive.pdd.data.model.Recommendation;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.activity.StatsActivity;
import org.stepik.android.adaptive.pdd.ui.adapter.QuizCardsAdapter;
import org.stepik.android.adaptive.pdd.ui.animation.CardsFragmentAnimations;
import org.stepik.android.adaptive.pdd.ui.dialog.DailyRewardDialog;
import org.stepik.android.adaptive.pdd.ui.dialog.ExpLevelDialog;
import org.stepik.android.adaptive.pdd.ui.dialog.RateAppDialog;
import org.stepik.android.adaptive.pdd.ui.dialog.StreakRestoreDialog;
import org.stepik.android.adaptive.pdd.ui.helper.CardHelper;
import org.stepik.android.adaptive.pdd.ui.listener.AnswerListener;
import org.stepik.android.adaptive.pdd.util.DailyRewardManager;
import org.stepik.android.adaptive.pdd.util.ExpUtil;
import org.stepik.android.adaptive.pdd.util.InventoryUtil;
import org.stepik.android.adaptive.pdd.util.RateAppUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static android.app.Activity.RESULT_OK;


public final class CardsFragment extends Fragment implements AnswerListener {
    public final static int STREAK_RESTORE_REQUEST_CODE = 3423;
    public final static String STREAK_RESTORE_KEY = "streak";

    private final static String TAG = "CardsFragment";

    private static final String LEVEL_DIALOG_TAG = "level_dialog";
    private static final String STREAK_RESTORE_DIALOG_TAG = "streak_restore_dialog";
    private static final String RATE_APP_DIALOG_TAG = "rate_app_dialog";
    private static final String DAILY_REWARD_DIALOG_TAG = "daily_reward_dialog";

    public static final String INVENTORY_DIALOG_TAG = "inventory_dialog";

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

        createReaction(0, RecommendationReaction.Reaction.INTERESTING);

        InventoryUtil.starterPack();

        resolveDailyReward();
    }

    private void resolveDailyReward() {
        final long progress = DailyRewardManager.INSTANCE.giveRewardAndGetCurrentRewardDay();
        if (progress != DailyRewardManager.getDISCARD())
            DailyRewardDialog.Companion.newInstance(progress).show(getChildFragmentManager(), DAILY_REWARD_DIALOG_TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommendations, container, false);

        binding.tryAgain.setOnClickListener(retrySubject::onNext);
        binding.courseCompletedText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.loadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);

        binding.progress.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        binding.toolbar.setOnClickListener((__) -> {
            AnalyticMgr.getInstance().statsOpened();
            startActivity(new Intent(getContext(), StatsActivity.class));
        });

        binding.cardsContainer.setAdapter(adapter);

        if (isCourseCompleted) {
            courseCompleted();
        } else {
            resubscribe();
            if (isError) {
                onError(null);
            }
        }

        binding.streakSuccessContainer.setNestedTextView(binding.streakSuccess);
        binding.streakSuccessContainer.setGradientDrawableParams(ContextCompat.getColor(getContext(), R.color.colorAccent), 0);

        updateExpProgressBar(ExpUtil.getExp(), 0, false);
        return binding.getRoot();
    }

    public void createReaction(final long lesson, final RecommendationReaction.Reaction reaction) {
        if (adapter.isEmptyOrContainsOnlySwipedCard(lesson) && binding != null) {
            binding.progress.setVisibility(View.VISIBLE);
            binding.loadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);
        }

        compositeDisposable.add(CardHelper.createReactionObservable(lesson, reaction, cards.size() + adapter.getItemCount())
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
        if (binding != null) {
            binding.expProgress.setMax((int) (next - prev));
            binding.expProgress.setProgress((int) (exp - prev));

            binding.expCounter.setText(Long.toString(exp)); //String.format(getString(R.string.exp_current_progress), exp - prev, next - prev));
            binding.expLevel.setText(String.format(getString(R.string.exp_title), level));
            binding.expLevelNext.setText(String.format(getString(R.string.exp_subtitle), next - exp));
        }

        if (showLevelDialog) {
            if (level != ExpUtil.getCurrentLevel(exp - streak)) {
                onLevelGained(level);
            }
        }
    }

    public void onCorrectAnswer(long submissionId) {
        final long streak = ExpUtil.incStreak();

        compositeDisposable.add(
                Completable.fromRunnable(() -> DataBaseMgr.getInstance().onExpGained(streak, submissionId))
                .subscribe(() -> {}, (e) -> {}));

        if (binding != null) {
            binding.expInc.setText(getString(R.string.exp_inc, streak));
            binding.streakSuccess.setText(getString(R.string.streak_success, streak));
            if (streak > 1) {
                CardsFragmentAnimations.playStreakSuccessAnimationSequence(binding);
            } else {
                CardsFragmentAnimations.playStreakBubbleAnimation(binding.expInc);
            }
        }

        if (RateAppUtil.onEngagement()) {
            new RateAppDialog().show(getChildFragmentManager(), RATE_APP_DIALOG_TAG);
        }

        updateExpProgressBar(ExpUtil.changeExp(streak), streak, true);
    }

    public void onWrongAnswer() {
        final long streak = ExpUtil.getStreak();
        if (streak > 1) {
            if (binding != null) {
                CardsFragmentAnimations.playStreakFailedAnimation(binding.streakFailed, binding.expProgress);
            }

            if (InventoryUtil.hasTickets()) {
                StreakRestoreDialog.Companion.newInstance(streak).show(getChildFragmentManager(), STREAK_RESTORE_DIALOG_TAG);
            }
        }
        ExpUtil.resetStreak();
    }

    private void onLevelGained(final long level) {
        ExpLevelDialog.Companion.newInstance(level).show(getChildFragmentManager(), LEVEL_DIALOG_TAG);
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
            binding.cardsContainer.setVisibility(View.GONE);
            binding.error.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

    /**
     * Retry to load a top card
     */
    private void retry() {
        isError = false;
        binding.error.setVisibility(View.GONE);
        binding.progress.setVisibility(View.VISIBLE);
        binding.cardsContainer.setVisibility(View.VISIBLE);
        binding.loadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);
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
        binding.progress.setVisibility(View.GONE);
        binding.cardsContainer.setVisibility(View.VISIBLE);
        cards.poll();
        resubscribe();
    }


    private void courseCompleted() {
        isCourseCompleted = true;
        if (binding != null) {
            binding.cardsContainer.setVisibility(View.GONE);
            binding.progress.setVisibility(View.GONE);
            binding.courseCompleted.setVisibility(View.VISIBLE);
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
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == STREAK_RESTORE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (binding != null) {
                CardsFragmentAnimations.playStreakRestoreAnimation(binding.streakSuccessContainer);
            }
            final long streak = data != null ? data.getLongExtra(STREAK_RESTORE_KEY, 0) : 0;
            ExpUtil.changeStreak(streak);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
