package org.stepik.android.adaptive.pdd.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.AttemptResponse;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.api.StepsResponse;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.Attempt;
import org.stepik.android.adaptive.pdd.data.model.Recommendation;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.data.model.Step;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.adapter.NavigationDrawerAdapter;
import org.stepik.android.adaptive.pdd.ui.adapter.QuizCardAdapter;
import org.stepik.android.adaptive.pdd.ui.listener.OnCardSwipeListener;

import io.reactivex.Observable;
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
    private NavigationDrawerAdapter navigationDrawerAdapter;

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
        navigationDrawerAdapter.bind(getActivity(), binding);

        binding.fragmentRecommendationsSolve.setOnClickListener((v) -> loadAttempt());
        binding.fragmentRecommendationsSubmit.setOnClickListener((v) -> makeSubmission());
        binding.fragmentRecommendationsNext.setOnClickListener((v) -> {
            reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.SOLVED));
            binding.fragmentRecommendationsContainer.swipeDown();
        });
        binding.fragmentRecommendationsTryAgain.setOnClickListener((v) -> {
            quizCardAdapter.setUIState(QuizCardAdapter.State.PENDING_FOR_NEXT_RECOMMENDATION);
            reactionSubject.onNext(reactionSubject.getValue());
        });

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
        navigationDrawerAdapter.unbind();

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> stepsSubject.onNext(res), this::handleError));


        reactionSubject =
                BehaviorSubject.createDefault(new RecommendationReaction(0, RecommendationReaction.Reaction.INTERESTING));
        onDestroyDisposable.add(reactionSubject
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(reaction -> {
                    final Observable<RecommendationsResponse> responseObservable = API.getInstance().getNextRecommendations();
                    if (reaction.getLesson() != 0) {
                        reaction.setUser(SharedPreferenceMgr.getInstance().getProfileId());
                        return API.getInstance().createReaction(reaction).andThen(responseObservable);
                    }
                    return responseObservable;
                })
                .subscribe((obs) -> onDestroyDisposable.add(obs
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::handleRecommendationsResponse, this::handleError))));

        attemptSubject = BehaviorSubject.create();
        submissionSubject = BehaviorSubject.create();


        navigationDrawerAdapter = new NavigationDrawerAdapter();
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
        quizCardAdapter = new QuizCardAdapter(this::onCardSwipe);
    }

    private void onCardSwipe(final OnCardSwipeListener.SWIPE_DIRECTION swipeDirection) {
        if (step == null) return;
        Log.d(TAG, "swipe " + swipeDirection);
        switch (swipeDirection) {
            case LEFT: // TOO EASY
                AnalyticMgr.getInstance().reactionEasy(step.getLesson());
                reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.NEVER_AGAIN));
            break;
            case RIGHT: // TOO HARD
                AnalyticMgr.getInstance().reactionHard(step.getLesson());
                reactionSubject.onNext(new RecommendationReaction(step.getLesson(), RecommendationReaction.Reaction.MAYBE_LATER));
            break;
        }
    }

    private void handleRecommendationsResponse(final RecommendationsResponse response) {
        final Recommendation recommendation = response.getFirstRecommendation();
        if (recommendation != null) {
            stepsBridgeSubject.onNext(recommendation.getLesson());
        } else if (response.getRecommendations().isEmpty()) {
            quizCardAdapter.setUIState(QuizCardAdapter.State.COURSE_COMPLETED);
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
                    .observeOn(AndroidSchedulers.mainThread())
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
        onDestroyDisposable.add(API.getInstance().createSubmission(submission)
                .andThen(API.getInstance().getSubmissions(submission.getAttempt()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> submissionSubject.onNext(res.getFirstSubmission()), this::handleError));
        quizCardAdapter.setUIState(QuizCardAdapter.State.PENDING_FOR_SUBMISSION);
    }
    private void handleSubmissionResponse(final Submission submission) {
        if (submission != null) {
            if (submission.getStatus() == Submission.Status.EVALUATION) {
                API.getInstance().getSubmissions(submission.getAttempt())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> submissionSubject.onNext(res.getFirstSubmission()), this::handleError);
            } else {
                AnalyticMgr.getInstance().answerResult(step, submission);
                quizCardAdapter.setSubmission(submission);
            }
        }
    }

    private void handleError(final Throwable throwable) {
        throwable.printStackTrace();
        quizCardAdapter.onError();
    }
}
