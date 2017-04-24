package org.stepik.droid.adaptive.pdd.ui.adapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.webkit.WebSettings;

import org.stepik.droid.adaptive.pdd.data.model.Attempt;
import org.stepik.droid.adaptive.pdd.data.model.Submission;
import org.stepik.droid.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.droid.adaptive.pdd.ui.DefaultWebViewClient;
import org.stepik.droid.adaptive.pdd.ui.helper.AnimationHelper;
import org.stepik.droid.adaptive.pdd.ui.helper.LayoutHelper;
import org.stepik.droid.adaptive.pdd.ui.listener.OnCardSwipeListener;

public final class QuizCardAdapter implements GestureDetector.OnGestureListener {
    private static final long ANIMATION_DURATION = 450;
    private static final long ANIMATION_DURATION_FAST = 300;
    private static final float MIN_SWIPE_DX = 350;
    private static final float MIN_SWIPE_SPEED = 300;

    private static final int ANSWER_ANIMATION_START = -160;
    private static final int ANSWER_ANIMATION_END = -32;
    private static final int SOLVE_BUTTON_OFFSET = 24;

    private ViewPropertyAnimator animatorHard;
    private ViewPropertyAnimator animatorEasy;

    private ValueAnimator answerAnimator, answerAnimatorReverse;

    private final String TAG = "QuizCardAdapter";

    private FragmentRecommendationsBinding binding;

    private final GestureDetector gestureDetector;
    private final OnCardSwipeListener listener;
    private final AttemptAnswersAdapter attemptAnswersAdapter;
    private final Context context;

    private final int screenHeight, screenWidth;

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


    
    public QuizCardAdapter(final Context context, final OnCardSwipeListener listener) {
        this.context = context;
        this.gestureDetector = new GestureDetector(context, this);
        this.listener = listener;
        this.attemptAnswersAdapter = new AttemptAnswersAdapter();

        this.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        this.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
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


        binding.fragmentRecommendationsLayout.setOnDispatchTouchListener((view, event) -> {
            if (state == State.PENDING_FOR_NEXT_RECOMMENDATION ||
                    state == State.SUBMISSION_CORRECT) return false;
            if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
                completeCardSwipe(0);
            }
            return true;
        });

        binding.fragmentRecommendationsCard.bringToFront();
        binding.fragmentRecommendationsSolve.bringToFront();

        final ValueAnimator.AnimatorUpdateListener answerAnimatorUpdateListener =
                AnimationHelper.createLayoutMarginAnimation(binding.fragmentRecommendationsAnswersContainer);

        answerAnimator = ValueAnimator.ofInt(
                LayoutHelper.pxFromDp(context, ANSWER_ANIMATION_START),
                LayoutHelper.pxFromDp(context, ANSWER_ANIMATION_END)
        );
        answerAnimator.setDuration(ANIMATION_DURATION_FAST);
        answerAnimator.addUpdateListener(answerAnimatorUpdateListener);

        answerAnimatorReverse = ValueAnimator.ofInt();
        answerAnimatorReverse.setDuration(ANIMATION_DURATION_FAST);
        answerAnimatorReverse.addUpdateListener((anm) ->
                binding.fragmentRecommendationsAnswersContainer.setAlpha(1f - (float) anm.getCurrentPlayTime() / anm.getDuration()));
        answerAnimatorReverse.addUpdateListener(answerAnimatorUpdateListener);


        attemptAnswersAdapter.setSubmitButton(binding.fragmentRecommendationsSubmit);
        binding.fragmentRecommendationsAnswers.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        binding.fragmentRecommendationsAnswers.setAdapter(attemptAnswersAdapter);

        setUIState(state);
    }

    public void unbind() {
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
                Log.d(TAG, "Wrong submission state: " + submission.getStatus());
        }
    }



    @Override
    public boolean onDown(MotionEvent motionEvent) {return false;}

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {return false;}

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float vX, float vY) {
        final float dx = e2.getX() - e1.getX();
        final float dy = e2.getY() - e1.getY();

        binding.fragmentRecommendationsContainer.setTranslationX(dx);
        binding.fragmentRecommendationsContainer.setTranslationY(dy);

        if (Math.abs(dx) > MIN_SWIPE_DX) {
            if (dx < 0) {
                if (animatorEasy == null) {
                    animatorEasy = AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsEasyReaction);
                }
            } else {
                if (animatorHard == null) {
                    animatorHard = AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsHardReaction);
                }
            }
        } else {
            hideReactionAnimation();
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
        if (Math.abs(vX) > 2 * Math.abs(vY)) {
            completeCardSwipe(vX);
            return true;
        } else {
            if (vY > 200) {
                if (binding.fragmentRecommendationsSolve.getVisibility() == View.VISIBLE) {
                    binding.fragmentRecommendationsSolve.callOnClick();
                }
                completeCardSwipe(0);
                return true;
            }
        }
        return false;
    }



    private void completeCardSwipe(float vX) {
        if (binding != null) {
            final float fromX = binding.fragmentRecommendationsContainer.getTranslationX();
            if (Math.abs(vX) > MIN_SWIPE_SPEED || Math.abs(fromX) > MIN_SWIPE_DX) {
                final OnCardSwipeListener.SWIPE_DIRECTION direction =
                        (vX > 0 || fromX > 0) ?
                                OnCardSwipeListener.SWIPE_DIRECTION.RIGHT :
                                OnCardSwipeListener.SWIPE_DIRECTION.LEFT;

                switch (direction) {
                    case LEFT:
                        if (animatorEasy == null) {
                            animatorEasy = AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsEasyReaction)
                                    .setDuration(ANIMATION_DURATION * 2)
                                    .withEndAction(this::hideReactionAnimation);
                        } else {
                            hideReactionAnimation();
                        }
                    break;
                    case RIGHT:
                        if (animatorHard == null) {
                            animatorHard = AnimationHelper.createReactionAppearAnimation(binding.fragmentRecommendationsHardReaction)
                                    .setDuration(ANIMATION_DURATION * 2)
                                    .withEndAction(this::hideReactionAnimation);
                        } else {
                            hideReactionAnimation();
                        }
                    break;
                }
                listener.onCardSwipe(direction);

                AnimationHelper.createTransitionAnimation(binding.fragmentRecommendationsContainer,
                        (direction == OnCardSwipeListener.SWIPE_DIRECTION.RIGHT ? 1 : -1) * 2 * screenWidth, 0)
                        .withEndAction(() -> setUIState(State.PENDING_FOR_NEXT_RECOMMENDATION))
                        .start();
            } else {
                AnimationHelper.playRollBackAnimation(binding.fragmentRecommendationsContainer);
            }
        }
    }

    private void hideReactionAnimation() {
        if (animatorEasy != null) {
            animatorEasy = null;
            AnimationHelper.createReactionDisappearAnimation(binding.fragmentRecommendationsEasyReaction).start();
        }
        if (animatorHard != null) {
            animatorHard = null;
            AnimationHelper.createReactionDisappearAnimation(binding.fragmentRecommendationsHardReaction).start();
        }
    }



    private void pendingForNextRecommendation() {
        binding.fragmentRecommendationsContainer.setTranslationX(0);
        binding.fragmentRecommendationsContainer.setTranslationY(-screenHeight);
        binding.fragmentRecommendationsProgressBar.setVisibility(View.VISIBLE);

        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.GONE);
        binding.fragmentRecommendationsSolve.setVisibility(View.VISIBLE);
    }

    private void recommendationLoaded() {
        binding.fragmentRecommendationsProgressBar.setVisibility(View.GONE);

        final ObjectAnimator animator =
                ObjectAnimator.ofFloat(binding.fragmentRecommendationsContainer, "translationY", 0);
        animator.setInterpolator(AnimationHelper.OvershootInterpolator2F);
        animator.addUpdateListener((anm) ->
            LayoutHelper.wrapWebView(binding.fragmentRecommendationsCard,
                    binding.fragmentRecommendationsQuestion, SOLVE_BUTTON_OFFSET));
        animator.start();
    }

    private void pendingForAnswers() {
        binding.fragmentRecommendationsSolve.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE);

        LayoutHelper.wrapWebView(binding.fragmentRecommendationsCard,
                binding.fragmentRecommendationsQuestion, 0);

        binding.fragmentRecommendationsSubmit.setAlpha(1);
        binding.fragmentRecommendationsSubmit.setEnabled(false);

        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsAnswers.setVisibility(View.GONE);

        binding.fragmentRecommendationsAnswersContainer.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsAnswersContainer.setAlpha(1);
        answerAnimator.start();
    }

    private void answersLoaded() {
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswers.setVisibility(View.VISIBLE);
    }

    private void pendingForSubmissions() {
        binding.fragmentRecommendationsSubmit.setEnabled(false);
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.VISIBLE);
        binding.fragmentRecommendationsAnswers.setVisibility(View.GONE);
    }

    private void submissionCorrect() {
        answerAnimatorReverse.setIntValues(
                LayoutHelper.pxFromDp(context, ANSWER_ANIMATION_END),
                -binding.fragmentRecommendationsAnswersContainer.getHeight());
        answerAnimatorReverse.start();

        binding.fragmentRecommendationsSubmit.animate()
                .alpha(0)
                .setDuration(ANIMATION_DURATION_FAST);

        binding.fragmentRecommendationsContainer.animate()
                .translationY(screenHeight)
                .setStartDelay(2 * ANIMATION_DURATION)
                .setDuration(2 * ANIMATION_DURATION)
                .withEndAction(() -> setUIState(State.PENDING_FOR_NEXT_RECOMMENDATION))
                .start();

        // todo show correct sign
    }

    private void submissionWrong() {
        AnimationHelper.playWiggleAnimation(binding.fragmentRecommendationsContainer);

        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswers.setVisibility(View.VISIBLE);

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
