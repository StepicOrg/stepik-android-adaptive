package org.stepik.android.adaptive.pdd.data.model;

import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.AttemptResponse;
import org.stepik.android.adaptive.pdd.ui.adapter.AttemptAnswersAdapter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Class that contains all card information
 *
 * Created by ruslandavletshin on 28/05/2017.
 */

public final class Card extends Observable<Card> {
    private final long lessonId;

    private Lesson lesson;
    private Disposable lessonDisposable;

    private Step step;
    private Disposable stepSubscription;

    private Attempt attempt;
    private Disposable attemptDisposable;

    private final AttemptAnswersAdapter adapter;

    private Throwable error;

    private Observer<? super Card> observer;

    private boolean correct = false;

    public Card(long lessonId, Lesson lesson, Step step, Attempt attempt) {
        this.lessonId = lessonId;
        this.lesson = lesson;
        this.step = step;
        this.attempt = attempt;
        this.adapter = new AttemptAnswersAdapter();
        adapter.setAttempt(attempt);
    }

    public Card(final long lessonId) {
        this.lessonId = lessonId;
        this.adapter = new AttemptAnswersAdapter();
    }

    public void init() {
        this.error = null;
        if (stepSubscription == null || (stepSubscription.isDisposed() && step == null)) {
            stepSubscription = API.getInstance().getSteps(lessonId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> setStep(res.getFirstStep()), this::onError);
        } else {
            setStep(step);
        }

        if (lessonDisposable == null || (lessonDisposable.isDisposed() && lesson == null)) {
            lessonDisposable = API.getInstance().getLessons(lessonId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> setLesson(res.getFirstLesson()), this::onError);
        }
    }

    public long getLessonId() {
        return lessonId;
    }

    private void setStep(Step step) {
        if (step != null) {
            this.step = step;
            if (attemptDisposable == null || (attemptDisposable.isDisposed() && attempt == null)) {
                attemptDisposable = Observable.concat(
                        API.getInstance().getAttempts(step.getId()),
                        API.getInstance().createAttempt(step.getId())
                )
                        .filter((r) -> r.getFirstAttempt() != null)
                        .take(1)
                        .map(AttemptResponse::getFirstAttempt)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::setAttempt, this::onError);
            }
            notifyDataChanged();
        }
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
        observer = null;
        adapter.clear();
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

    public AttemptAnswersAdapter getAdapter() {
        return adapter;
    }

    public void onCorrect() {
        this.correct = true;
    }

    public boolean isCorrect() {
        return correct;
    }
}
