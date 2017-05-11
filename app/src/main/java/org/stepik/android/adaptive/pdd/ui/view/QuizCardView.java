package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import org.stepik.android.adaptive.pdd.ui.helper.AnimationHelper;

public final class QuizCardView extends RelativeLayout {
    private float startX = 0;
    private float startY = 0;

    private float elemX;
    private float elemY;

    private final int screenWidth;
    private final int screenHeight;

    private final GestureDetector flingDetector;

    private final static float MIN_FLING_VELOCITY = 400;
    private final float MIN_FLING_TRANSLATION;

    private final float MIN_SWIPE_TRANSLATION;

    private QuizCardFlingListener listener;

    public QuizCardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.flingDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                onActionUp(vx, vy);
                return true;
            }
        });

        this.listener = new QuizCardFlingListener();

        this.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        this.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        MIN_SWIPE_TRANSLATION = this.screenWidth / 2;
        MIN_FLING_TRANSLATION = this.screenWidth / 4;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        processTouchEvent(motionEvent);
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        processTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void processTouchEvent(MotionEvent motionEvent) {
        boolean isFling = flingDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                startY = motionEvent.getY();

                elemX = getTranslationX();
                elemY = getTranslationY();

                getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                if (!isFling) {
                    onActionUp(0, 0);
                }
                getParent().requestDisallowInterceptTouchEvent(false);

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getX() - startX;
                float dy = motionEvent.getY() - startY;

                elemX += dx;
                elemY += dy;

                setTranslationX(elemX);
                setTranslationY(elemY);

                listener.onScroll(elemX / screenWidth);
                break;
        }
    }

    private void onActionUp(final float vx, final float vy) {
        final float x = getTranslationX();
//        final float y = getTranslationY();

        if (Math.abs(x) > MIN_FLING_TRANSLATION && Math.abs(vx) > MIN_FLING_VELOCITY && Math.abs(vx) > Math.abs(vy)
                || Math.abs(x) > MIN_SWIPE_TRANSLATION) {

            if (x > 0) {
                listener.onSwipeRight();
            } else {
                listener.onSwipeLeft();
            }
            AnimationHelper.createTransitionAnimation(this, Math.signum(x) * 2 * screenWidth, 0).withEndAction(listener::onSwiped);
        } else {
            if (Math.abs(vy) > MIN_FLING_VELOCITY) {
                listener.onFlingDown();
            }

            AnimationHelper.playRollBackAnimation(this);
        }
    }

    public void swipeDown() {
        listener.onSwipeDown();
        AnimationHelper.createTransitionAnimation(this, 0, screenHeight)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(listener::onSwiped);
    }

    public void setQuizCardFlingListener(final QuizCardFlingListener listener) {
        this.listener = listener;
    }

    public static class QuizCardFlingListener {
        public void onSwiped() {}
        public void onFlingDown() {}
        public void onSwipeLeft() {}
        public void onSwipeRight() {}
        public void onSwipeDown() {}

        /**
         *
         * @param scrollProgress - represents scroll progress for current state, e.g. 1.0 when card completely swiped to right
         */
        public void onScroll(final float scrollProgress) {}

    }
}
