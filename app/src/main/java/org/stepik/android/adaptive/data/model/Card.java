package org.stepik.android.adaptive.data.model;

import org.stepik.android.adaptive.App;
import org.stepik.android.adaptive.api.Api;
import org.stepik.android.adaptive.api.AttemptResponse;
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler;
import org.stepik.android.adaptive.di.qualifiers.MainScheduler;
import org.stepik.android.adaptive.resolvers.StepTypeResolver;
import org.stepik.android.adaptive.ui.adapter.attempts.AttemptAnswerAdapter;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * Class that contains all card information
 *
 * Created by ruslandavletshin on 28/05/2017.
 */

public final class Card extends Observable<Card> {
    public static final long MOCK_LESSON_ID = -1;

    private final long lessonId;

    private Lesson lesson;
    private Disposable lessonDisposable;

    private Step step;
    private Disposable stepSubscription;

    private Attempt attempt;
    private Disposable attemptDisposable;

    private AttemptAnswerAdapter adapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Throwable error;

    private Observer<? super Card> observer;

    private boolean correct = false;

    @Inject
    public Api api;

    @Inject
    public StepTypeResolver stepTypeResolver;

    @Inject
    @MainScheduler
    public Scheduler mainScheduler;

    @Inject
    @BackgroundScheduler
    public Scheduler backgroundScheduler;

    public Card(long lessonId, Lesson lesson, Step step, Attempt attempt) {
        injectComponent();
        this.lessonId = lessonId;
        this.lesson = lesson;
        this.step = step;
        this.attempt = attempt;
        adapter = stepTypeResolver.getAttemptAdapter(step);
        adapter.setAttempt(attempt);
    }

    public Card(final long lessonId) {
        injectComponent();
        this.lessonId = lessonId;
    }

    private void injectComponent() {
        App.Companion.componentManager().getStudyComponent().inject(this);
    }

    public void init() {
        this.error = null;
        if (stepSubscription == null || (stepSubscription.isDisposed() && step == null)) {
            stepSubscription = api.getSteps(lessonId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe(res -> setStep(res.getFirstStep()), this::onError);
        } else {
            setStep(step);
        }

        if (lessonDisposable == null || (lessonDisposable.isDisposed() && lesson == null)) {
            lessonDisposable = api.getLessons(lessonId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe(res -> setLesson(res.getFirstLesson()), this::onError);
        }
    }

    public long getLessonId() {
        return lessonId;
    }

    private void setStep(Step step) {
        if (step != null) {
            this.step = step;
            this.adapter = stepTypeResolver.getAttemptAdapter(step);
            if (attemptDisposable == null || (attemptDisposable.isDisposed() && attempt == null)) {
                attemptDisposable = Observable.concat(
                        api.getAttempts(step.getId()),
                        api.createAttempt(step.getId())
                )
                        .filter((r) -> r.getFirstAttempt() != null)
                        .take(1)
                        .map(AttemptResponse::getFirstAttempt)
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(this::setAttempt, this::onError);
            }

            compositeDisposable.add(api.getUnits(lessonId)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(backgroundScheduler)
                    .subscribe((res) -> reportView(res.getTopUnit(), step.getId()), (__) -> {}));
            notifyDataChanged();
        }
    }

    private void reportView(final Unit unit, final long stepId) {
        if (unit == null) return;
        final long assignment = unit.getTopAssignment();
        if (assignment == 0) return;

        compositeDisposable.add(api.reportView(assignment, stepId)
                .subscribeOn(backgroundScheduler)
                .observeOn(backgroundScheduler)
                .subscribe(() -> {}, (__) -> {}));
    }

    private void setLesson(Lesson lesson) {
        if (lesson != null) {
            this.lesson = lesson;
            notifyDataChanged();
        }
    }

    private void setAttempt(Attempt attempt) {
        if (attempt != null) {
            this.attempt = attempt;
            adapter.setAttempt(attempt);
            notifyDataChanged();
        }
    }

    private void onError(final Throwable error) {
        this.error = error;
        notifyDataChanged();
    }

    private void notifyDataChanged() {
        if (observer != null) {
            if (error != null) {
                observer.onError(error);
            }
            if (lesson != null && attempt != null && step != null) {
                observer.onNext(this);
                observer.onComplete();
            }
        }
    }

    /**
     * Free resources
     */
    public void recycle() {
        if (lessonDisposable != null) lessonDisposable.dispose();
        if (stepSubscription != null) stepSubscription.dispose();
        if (attemptDisposable != null) attemptDisposable.dispose();
        compositeDisposable.dispose();
        observer = null;
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Override
    protected void subscribeActual(Observer<? super Card> observer) {
        this.observer = observer;
        init();
        notifyDataChanged();
    }

    public Lesson getLesson() {
        return lesson;
    }

    public Step getStep() {
        return step;
    }

    public AttemptAnswerAdapter getAdapter() {
        return adapter;
    }

    public void onCorrect() {
        this.correct = true;
    }

    public boolean isCorrect() {
        return correct;
    }
}
