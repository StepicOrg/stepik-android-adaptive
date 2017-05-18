package org.stepik.android.adaptive.pdd.ui.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.data.model.Attempt;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.DefaultWebViewClient;
import org.stepik.android.adaptive.pdd.ui.helper.AnimationHelper;
import org.stepik.android.adaptive.pdd.ui.helper.LayoutHelper;
import org.stepik.android.adaptive.pdd.ui.listener.OnCardSwipeListener;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardView;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public final class QuizCardAdapter {
    private static final int ANSWER_ANIMATION_START = -LayoutHelper.pxFromDp(160);
    private static final int ANSWER_ANIMATION_END = 0;


    private ValueAnimator answerAnimator, answerAnimatorReverse;

    private final String TAG = "QuizCardAdapter";

    private FragmentRecommendationsBinding binding;

    private final OnCardSwipeListener listener;
    private final AttemptAnswersAdapter attemptAnswersAdapter;

    private final PublishSubject<Float> scrollSubject = PublishSubject.create();
    private Disposable scrollDisposable;

    private final int screenHeight;

    public enum State {
        PENDING_FOR_NEXT_RECOMMENDATION,
        RECOMMENDATION_LOADED,
        PENDING_FOR_ANSWERS,
        ANSWERS_LOADED,
        PENDING_FOR_SUBMISSION,
        SUBMISSION_CORRECT,
        SUBMISSION_WRONG
    }

    private State state = State.PENDING_FOR_NEXT_RECOMMENDATION;

    
    public QuizCardAdapter(final OnCardSwipeListener listener) {
        this.listener = listener;
        this.attemptAnswersAdapter = new AttemptAnswersAdapter();

        this.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void bind(final FragmentRecommendationsBinding binding) {
        this.binding = binding;

        final WebSettings settings = binding.fragmentRecommendationsQuestion.getSettings();
        settings.setAllowContentAccess(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);


        binding.fragmentRecommendationsQuestion.setWebViewClient(new DefaultWebViewClient(null, (v, u) ->
                setUIState(State.RECOMMENDATION_LOADED)));
        binding.fragmentRecommendationsQuestion.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        scrollDisposable = scrollSubject
            .throttleFirst(AnimationHelper.ANIMATION_DURATION_FAST, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe((scrollProgress) -> {
                if (Math.abs(scrollProgress) > 0.5) {
                    AnimationHelper.createReactionAppearAnimation(
                            scrollProgress < 0 ? binding.fragmentRecommendationsEasyReaction : binding.fragmentRecommendationsHardReaction
                    );
                } else {
                    hideReactionAnimation(0);
                }
            });

        binding.fragmentRecommendationsContainer.setQuizCardFlingListener(new QuizCardView.QuizCardFlingListener() {
            @Override
            public void onFlingDown() {
                if (binding.fragmentRecommendationsSolve.getVisibility() == View.VISIBLE) {
                    binding.fragmentRecommendationsSolve.callOnClick();
                }
            }

            @Override
            public void onScroll(final float scrollProgress) {
                scrollSubject.onNext(scrollProgress);
            }

            @Override
            public void onSwiped() {
                setUIState(State.PENDING_FOR_NEXT_RECOMMENDATION);
            }

            @Override
            public void onSwipeLeft() {
                AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsEasyReaction)
                        .withEndAction(() -> hideReactionAnimation(AnimationHelper.ANIMATION_DURATION * 2));
                listener.onCardSwipe(OnCardSwipeListener.SWIPE_DIRECTION.LEFT);
            }

            @Override
            public void onSwipeRight() {
                AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsHardReaction)
                        .withEndAction(() -> hideReactionAnimation(AnimationHelper.ANIMATION_DURATION * 2));
                listener.onCardSwipe(OnCardSwipeListener.SWIPE_DIRECTION.RIGHT);
            }

            @Override
            public void onSwipeDown() {
                AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsCorrectReaction)
                        .withEndAction(() -> hideReactionAnimation(AnimationHelper.ANIMATION_DURATION * 2));
            }
        });

        final ValueAnimator.AnimatorUpdateListener answerAnimatorUpdateListener =
                AnimationHelper.createPaddingAnimation(binding.fragmentRecommendationsAnswersContainer);

        answerAnimator = ValueAnimator.ofInt(ANSWER_ANIMATION_START, ANSWER_ANIMATION_END);
        answerAnimator.setDuration(AnimationHelper.ANIMATION_DURATION_FAST);
        answerAnimator.addUpdateListener(answerAnimatorUpdateListener);

        answerAnimatorReverse = ValueAnimator.ofInt();
        answerAnimatorReverse.setDuration(AnimationHelper.ANIMATION_DURATION_FAST);
//        answerAnimatorReverse.addUpdateListener((anm) ->
//                binding.fragmentRecommendationsAnswersContainer.setAlpha(1f - (float) anm.getCurrentPlayTime() / anm.getDuration()));
        answerAnimatorReverse.addUpdateListener(answerAnimatorUpdateListener);
        answerAnimatorReverse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                binding.fragmentRecommendationsContainer.swipeDown();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        attemptAnswersAdapter.setSubmitButton(binding.fragmentRecommendationsSubmit);
        binding.fragmentRecommendationsAnswers.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        binding.fragmentRecommendationsAnswers.setAdapter(attemptAnswersAdapter);

        setUIState(state);
    }

    public void unbind() {
        scrollDisposable.dispose();
        binding = null;
    }



    public void setAttempt(final Attempt attempt) {
        attemptAnswersAdapter.setAttempt(attempt);
        setUIState(State.ANSWERS_LOADED);
    }

    public Submission getSubmission() { return attemptAnswersAdapter.getSubmission(); }

    public void setSubmission(final Submission submission) {
        switch (submission.getStatus()) {
            case CORRECT:
                setUIState(State.SUBMISSION_CORRECT);
            break;
            case WRONG:
                setUIState(State.SUBMISSION_WRONG);
            break;
            default:
                Log.e(TAG, "Wrong submission state: " + submission.getStatus());
        }
    }

    private void hideReactionAnimation(final long delay) {
        AnimationHelper.createReactionDisappearAnimation(binding.fragmentRecommendationsEasyReaction)
                .setStartDelay(delay);
        AnimationHelper.createReactionDisappearAnimation(binding.fragmentRecommendationsHardReaction)
                .setStartDelay(delay);
        AnimationHelper.createReactionDisappearAnimation(binding.fragmentRecommendationsCorrectReaction)
                .setStartDelay(delay);
    }


    private void pendingForNextRecommendation() {
        binding.fragmentRecommendationsContainer.setTranslationX(0);
        binding.fragmentRecommendationsContainer.setTranslationY(-screenHeight);
        binding.fragmentRecommendationsProgressBar.setVisibility(View.VISIBLE);

        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsSolve.setVisibility(View.VISIBLE);
    }

    private void recommendationLoaded() {
        binding.fragmentRecommendationsProgressBar.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);

        final ObjectAnimator animator =
                ObjectAnimator.ofFloat(binding.fragmentRecommendationsContainer, "translationY", 0);
        animator.setInterpolator(AnimationHelper.OvershootInterpolator2F);
        animator.setDuration(AnimationHelper.ANIMATION_DURATION);
        animator.start();
    }

    private void pendingForAnswers() {
        binding.fragmentRecommendationsSolve.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE);

        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setEnabled(false);

    }

    private void answersLoaded() {
        answerAnimator.start();
        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);

        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE);
    }

    private void pendingForSubmissions() {
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.VISIBLE);

        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);
    }

    private void submissionCorrect() {
        answerAnimatorReverse.setIntValues(
                ANSWER_ANIMATION_END,
                -binding.fragmentRecommendationsAnswersContainer.getHeight());
        answerAnimatorReverse.start();

        binding.fragmentRecommendationsSubmit.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.animate()
                .setDuration(AnimationHelper.ANIMATION_DURATION_FAST)
                .withEndAction(binding.fragmentRecommendationsContainer::swipeDown);
    }

    private void submissionWrong() {
        AnimationHelper.playWiggleAnimation(binding.fragmentRecommendationsContainer);

        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE);

        Snackbar.make(binding.getRoot(), R.string.wrong, Snackbar.LENGTH_SHORT).show();
    }

    public void setUIState(final State state) {
        Log.d(TAG, "Switch state: " + state);
        if (binding == null) return;
        switch (state) {
            case PENDING_FOR_NEXT_RECOMMENDATION:
                pendingForNextRecommendation();
            break;
            case RECOMMENDATION_LOADED:
                recommendationLoaded();
            break;
            case PENDING_FOR_ANSWERS:
                pendingForAnswers();
            break;
            case ANSWERS_LOADED:
                answersLoaded();
            break;
            case PENDING_FOR_SUBMISSION:
                pendingForSubmissions();
            break;
            case SUBMISSION_CORRECT:
                submissionCorrect();
            break;
            case SUBMISSION_WRONG:
                submissionWrong();
            break;
        }
        this.state = state;
    }
}
