package org.stepik.droid.adaptive.pdd.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.Util;
import org.stepik.droid.adaptive.pdd.api.API;
import org.stepik.droid.adaptive.pdd.api.AttemptResponse;
import org.stepik.droid.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.droid.adaptive.pdd.api.StepsResponse;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.data.model.Attempt;
import org.stepik.droid.adaptive.pdd.data.model.Recommendation;
import org.stepik.droid.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.droid.adaptive.pdd.data.model.Step;
import org.stepik.droid.adaptive.pdd.data.model.Submission;
import org.stepik.droid.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.droid.adaptive.pdd.ui.adapter.QuizCardAdapter;
import org.stepik.droid.adaptive.pdd.ui.listener.OnCardSwipeListener;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Response;

public final class RecommendationsFragment extends Fragment {
    private final static String TAG = "RecommendationsFragment";
    private FragmentRecommendationsBinding binding;
    private QuizCardAdapter quizCardAdapter;

    private Step step;

    private BehaviorSubject<Response<StepsResponse>> stepsSubject;
    private BehaviorSubject<Attempt> attemptSubject;
    private PublishSubject<Long> stepsBridgeSubject;
    private BehaviorSubject<RecommendationReaction> reactionSubject;
    private BehaviorSubject<Submission> submissionSubject;

    private CompositeDisposable viewDisposable, onDestroyDisposable;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommendations, container, false);
        quizCardAdapter.bind(binding);

        binding.fragmentRecommendationsSolve.setOnClickListener((v) -> loadAttempt());
        binding.fragmentRecommendationsSubmit.setOnClickListener((v) -> makeSubmission());

        viewDisposable.add(stepsSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleStepsResponse, this::handleError));

        viewDisposable.add(attemptSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleAttemptResponse, this::handleError));

        viewDisposable.add(submissionSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSubmissionResponse, this::handleError));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewDisposable.dispose();

        quizCardAdapter.unbind();

        binding.unbind();
        binding = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        viewDisposable = new CompositeDisposable();
        onDestroyDisposable = new CompositeDisposable();

        stepsSubject = BehaviorSubject.create();

        stepsBridgeSubject = PublishSubject.create();
        onDestroyDisposable.add(stepsBridgeSubject.observeOn(Schedulers.io())
                .map(lesson_id -> API.getInstance().getSteps(lesson_id).execute())
                .subscribe(res -> stepsSubject.onNext(res), this::handleError));


        reactionSubject =
                BehaviorSubject.createDefault(new RecommendationReaction(0, RecommendationReaction.Reaction.INTERESTING));
        onDestroyDisposable.add(reactionSubject
                .observeOn(Schedulers.io())
                .map(reaction -> {
                    if (reaction.getLesson() != 0) {
                        reaction.setUser(SharedPreferenceMgr.getInstance().getLong(SharedPreferenceMgr.PROFILE_ID));
                        API.getInstance().createReaction(reaction).execute();
                    }
                    return API.getInstance().getNextRecommendations().execute();
                })
                .observeOn(Schedulers.computation())
                .subscribe(this::handleRecommendationsResponse, this::handleError));

        attemptSubject = BehaviorSubject.create();
        submissionSubject = BehaviorSubject.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroyDisposable.dispose();
        onDestroyDisposable = null;

        quizCardAdapter = null;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        quizCardAdapter = new QuizCardAdapter(context, this::onCardSwipe);
    }

    private void onCardSwipe(final OnCardSwipeListener.SWIPE_DIRECTION swipeDirection) {
        if (step == null) return;
        Log.d(TAG, "swipe " + swipeDirection);
        switch (swipeDirection) {
            case LEFT: // TOO EASY
                reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.NEVER_AGAIN));
            break;
            case RIGHT: // TOO HARD
                reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.MAYBE_LATER));
            break;
        }
    }

    private void handleRecommendationsResponse(final Response<RecommendationsResponse> response) {
        if (response.isSuccessful()) {
            final Recommendation recommendation = response.body().getFirstRecommendation();
            if (recommendation != null) {
                stepsBridgeSubject.onNext(recommendation.getLesson());
            }
        }
    }
    private void handleStepsResponse(final Response<StepsResponse> response) {
        if (response.isSuccessful()) {
            final Step step = response.body().getFirstStep();
            if (step != null && step.getBlock() != null && step.getBlock().getText() != null) {
                this.step = step;
                binding.fragmentRecommendationsQuestion
                        .loadDataWithBaseURL("file:///android_asset/css/", Util.prepareHTML(step.getBlock().getText()), "text/html", "UTF-8", null);
            }
        }
    }


    private void loadAttempt() {
        if (step != null) {
            quizCardAdapter.setUIState(QuizCardAdapter.State.PENDING_FOR_ANSWERS);

            Observable.concat(
                    API.getInstance().getAttempts(step.getId()),
                    API.getInstance().createAttempt(step.getId())
            )
                    .filter((r) -> r.getFirstAttempt() != null)
                    .take(1)
                    .map(AttemptResponse::getFirstAttempt)
                    .subscribeOn(Schedulers.io())
                    .subscribe(attemptSubject::onNext, this::handleError);
        }
    }
    private void handleAttemptResponse(final Attempt attempt) {
        if (attempt != null) {
            quizCardAdapter.setAttempt(attempt);
        }
    }


    private void makeSubmission() {
        final Submission submission = quizCardAdapter.getSubmission();
        onDestroyDisposable.add(Observable.concat(
                API.getInstance().createSubmission(submission),
                API.getInstance().getSubmissions(submission.getAttempt())
        )
                .skip(1)
                .take(1)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> submissionSubject.onNext(res.getFirstSubmission()), this::handleError));
        quizCardAdapter.setUIState(QuizCardAdapter.State.PENDING_FOR_SUBMISSION);
    }
    private void handleSubmissionResponse(final Submission submission) {
        if (submission != null) {
            if (submission.getStatus() == Submission.Status.EVALUATION) {
                API.getInstance().getSubmissions(submission.getAttempt())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(res -> submissionSubject.onNext(res.getFirstSubmission()), this::handleError);
            } else {
                if (submission.getStatus() == Submission.Status.CORRECT) {
                    reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.SOLVED));
                }
                quizCardAdapter.setSubmission(submission);
            }
        }
    }

    private void handleError(final Throwable throwable) {
        throwable.printStackTrace();
    }
}
