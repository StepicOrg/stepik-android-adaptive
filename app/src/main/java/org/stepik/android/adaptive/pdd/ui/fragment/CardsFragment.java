package org.stepik.android.adaptive.pdd.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.api.SubmissionResponse;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.model.Card;
import org.stepik.android.adaptive.pdd.data.model.Recommendation;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.DefaultWebViewClient;
import org.stepik.android.adaptive.pdd.ui.dialog.LogoutDialog;
import org.stepik.android.adaptive.pdd.ui.helper.AnimationHelper;
import org.stepik.android.adaptive.pdd.ui.helper.CardHelper;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardView;
import org.stepik.android.adaptive.pdd.util.HtmlUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public final class CardsFragment extends Fragment {
    private final static String TAG = "CardsFragment";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final PublishSubject<View> retrySubject = PublishSubject.create();

    private final Queue<Card> cards = new ArrayDeque<>();

    private String[] loadingPlaceholders;

    private FragmentRecommendationsBinding binding;

    private Disposable cardDisposable;

    private Submission submission;

    private boolean isError = false;
    private boolean isCourseCompleted = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        compositeDisposable.add(retrySubject.observeOn(AndroidSchedulers.mainThread()).subscribe(v -> retry()));
        loadingPlaceholders = getResources().getStringArray(R.array.recommendation_loading_placeholders);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommendations, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.fragmentRecommendationsToolbar);

        if (savedInstanceState == null) {
            createReaction(0, RecommendationReaction.Reaction.INTERESTING);
        }

        binding.fragmentRecommendationsCourseCompletedText.setMovementMethod(LinkMovementMethod.getInstance());

        final WebSettings settings = binding.fragmentRecommendationsQuestion.getSettings();
        settings.setAllowContentAccess(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        binding.fragmentRecommendationsQuestion.setWebViewClient(new DefaultWebViewClient(null, (v, u) -> onCardLoaded()));
        binding.fragmentRecommendationsQuestion.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        binding.fragmentRecommendationsAnswers.setNestedScrollingEnabled(false);
        binding.fragmentRecommendationsAnswers.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fragmentRecommendationsTryAgain.setOnClickListener(retrySubject::onNext);
        binding.fragmentRecommendationsNext.setOnClickListener(v -> binding.fragmentRecommendationsContainer.swipeDown());
        binding.fragmentRecommendationsSubmit.setOnClickListener(v -> createSubmission());
        binding.fragmentRecommendationsWrongRetry.setOnClickListener(v -> {
            submission = null;
            cards.peek().getAdapter().setEnabled(true);
            CardHelper.resetSupplementalActions(binding);
        });
        binding.fragmentRecommendationsLoadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);

        CardHelper.resetCard(binding);

        binding.fragmentRecommendationsContainer.setQuizCardFlingListener(new QuizCardView.QuizCardFlingListener() {
            @Override
            public void onScroll(float scrollProgress) {
                binding.fragmentRecommendationsHardReaction.setAlpha(Math.max(2 * scrollProgress, 0));
                binding.fragmentRecommendationsEasyReaction.setAlpha(Math.max(2 * -scrollProgress, 0));
            }

            @Override
            public void onSwipeLeft() {
                binding.fragmentRecommendationsEasyReaction.setAlpha(1);
                createReaction(cards.peek().getStep().getLesson(), RecommendationReaction.Reaction.NEVER_AGAIN);
                AnalyticMgr.getInstance().reactionEasy(cards.peek().getStep().getLesson());
            }

            @Override
            public void onSwipeRight() {
                binding.fragmentRecommendationsHardReaction.setAlpha(1);
                createReaction(cards.peek().getStep().getLesson(), RecommendationReaction.Reaction.MAYBE_LATER);
                AnalyticMgr.getInstance().reactionHard(cards.peek().getStep().getLesson());
            }

            @Override
            public void onSwiped() {
                binding.fragmentRecommendationsEasyReaction.setAlpha(0);
                binding.fragmentRecommendationsHardReaction.setAlpha(0);

                CardHelper.resetCard(binding);
                cards.poll().recycle();
                submission = null;
                resubscribe();
            }
        });

        if (isCourseCompleted) {
            courseCompleted();
        } else {
            resubscribe();
            onSubmission(submission, false);
            if (isError) {
                onError(null);
            }
        }
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cards_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            FragmentMgr.getInstance().showDialog(new LogoutDialog());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createReaction(final long lesson, final RecommendationReaction.Reaction reaction) {
        binding.fragmentRecommendationsProgress.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsLoadingPlaceholder.setText(loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]);
        compositeDisposable.add(CardHelper.createReactionObservable(lesson, reaction)
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
            CardHelper.resetCard(binding);
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
        binding.fragmentRecommendationsTitle.setText(card.getLesson().getTitle());
        HtmlUtil.setCardWebViewHtml(
                binding.fragmentRecommendationsQuestion,
                HtmlUtil.prepareCardHtml(card.getStep().getBlock().getText()));


        binding.fragmentRecommendationsAnswers.setAdapter(card.getAdapter());
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE); // set button
        card.getAdapter().setSubmitButton(binding.fragmentRecommendationsSubmit);
    }


    /**
     * Called when card completely loaded and ready
     */
    private void onCardLoaded() {
        if (binding == null) return;
        binding.fragmentRecommendationsProgress.setVisibility(View.GONE); // hide progresses
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);

        CardHelper.scrollDown(binding.fragmentRecommendationsScroll);
        CardHelper.showCard(binding.fragmentRecommendationsContainer);
    }


    private void createSubmission() {
        CardHelper.resetSupplementalActions(binding);

        binding.fragmentRecommendationsContainer.setEnabled(false);
        binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.VISIBLE);
        cards.peek().getAdapter().setEnabled(false);

        CardHelper.scrollDown(binding.fragmentRecommendationsScroll);

        final Submission submission = cards.peek().getAdapter().getSubmission();

        compositeDisposable.add(
                API.getInstance().createSubmission(submission)
                .andThen(API.getInstance().getSubmissions(submission.getAttempt()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSubmissionLoaded, this::onSubmissionError)
        );
    }

    private void onSubmissionLoaded(final SubmissionResponse response) {
        final Submission submission = response.getFirstSubmission();
        this.submission = submission;
        if (submission.getStatus() == Submission.Status.EVALUATION) {
            compositeDisposable.add( // retry to reload submission
                    API.getInstance().getSubmissions(submission.getAttempt())
                            .delay(1, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::onSubmissionLoaded, this::onSubmissionError)
            );
        } else {
            AnalyticMgr.getInstance().answerResult(cards.peek().getStep(), submission);
            if (submission.getStatus() == Submission.Status.CORRECT) {
                createReaction(cards.peek().getStep().getLesson(), RecommendationReaction.Reaction.SOLVED);
            }
            if (binding != null) onSubmission(submission, true);
        }
    }

    private void onSubmission(final Submission submission, final boolean animate) {
        if (submission == null) return;
        CardHelper.resetSupplementalActions(binding);
        switch (submission.getStatus()) {
            case CORRECT:
                binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);

                binding.fragmentRecommendationsCorrect.setVisibility(View.VISIBLE);
                binding.fragmentRecommendationsNext.setVisibility(View.VISIBLE);
                binding.fragmentRecommendationsContainer.setEnabled(false);

                binding.fragmentRecommendationsHint.setText(submission.getHint());
                binding.fragmentRecommendationsHint.setVisibility(View.VISIBLE);

                if (animate) {
                    CardHelper.scrollDown(binding.fragmentRecommendationsScroll);
                }
                break;
            case WRONG:
                binding.fragmentRecommendationsWrong.setVisibility(View.VISIBLE);
                binding.fragmentRecommendationsWrongRetry.setVisibility(View.VISIBLE);
                binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);

                binding.fragmentRecommendationsContainer.setEnabled(true);

                if (animate) {
                    AnimationHelper.playWiggleAnimation(binding.fragmentRecommendationsContainer);
                }
            break;
        }
    }

    private void onSubmissionError(final Throwable error) {
        submission = null;
        cards.peek().getAdapter().setEnabled(true);
        if (binding != null) {
            binding.fragmentRecommendationsContainer.setEnabled(true);
            Snackbar.make(binding.getRoot(), R.string.network_error, Snackbar.LENGTH_SHORT).show();
            CardHelper.resetSupplementalActions(binding);
        }
    }

    private void courseCompleted() {
        isCourseCompleted = true;
        if (binding != null) {
            binding.fragmentRecommendationsContainer.setVisibility(View.GONE);
            binding.fragmentRecommendationsProgress.setVisibility(View.GONE);
            binding.fragmentRecommendationsCourseCompleted.setVisibility(View.VISIBLE);
        }
    }

    private boolean isCardExists(final long lessonId) {
        for (final Card card : cards) {
            if (card.getLessonId() == lessonId) return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        if (cardDisposable != null) cardDisposable.dispose();
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        for (final Card card : cards) {
            card.recycle();
        }
        super.onDestroy();
    }
}
