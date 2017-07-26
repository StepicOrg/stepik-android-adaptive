package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.ui.helper.AnimationHelper;

import java.util.HashSet;
import java.util.Set;

public final class SwipeableLayout extends FrameLayout {
    private float startX = 0;
    private float startY = 0;

    private float elemX;
    private float elemY;

    private final int screenWidth;
    private final int screenHeight;
    private final float viewX;

    private final GestureDetector flingDetector;

    private final static float MIN_FLING_VELOCITY = 400;
    private final static float ROTATION_ANGLE = 15.5f;

    private final static float MIN_DELTA = 32;

    private final static int TOUCH_ABOVE = 0;
    private final static int TOUCH_BELOW = 1;
    private int touchPosition;

    private boolean intercepted = false;

    private final float MIN_FLING_TRANSLATION;

    private final float MIN_SWIPE_TRANSLATION;

    private Set<SwipeListener> listeners = new HashSet<>();

    public SwipeableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.flingDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                onActionUp(vx, vy);
                return true;
            }
        });

//        this.listener = new SwipeListener();

        this.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        this.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        this.viewX = getX();

        MIN_SWIPE_TRANSLATION = this.screenWidth / 2;
        MIN_FLING_TRANSLATION = this.screenWidth / 4;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        processTouchEvent(motionEvent);
        return intercepted || super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        processTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && isEnabled()) {
            return true;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void processTouchEvent(MotionEvent motionEvent) {
        if (!this.isEnabled()) return;
        boolean isFling = flingDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                startY = motionEvent.getY();

                elemX = getTranslationX();
                elemY = getTranslationY();

                if (startY < getHeight() / 2) {
                    touchPosition = TOUCH_ABOVE;
                } else {
                    touchPosition = TOUCH_BELOW;
                }

                getParent().requestDisallowInterceptTouchEvent(true);
                intercepted = false;
                break;

            case MotionEvent.ACTION_UP:
                if (!isFling) {
                    onActionUp(0, 0);
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = motionEvent.getX() - startX;
                float dy = motionEvent.getY() - startY;

                intercepted = intercepted || Math.abs(dx) > MIN_DELTA || Math.abs(dy) > MIN_DELTA;

                if (intercepted) {
                    elemX += dx;
                    elemY += dy;

                    setTranslationX(elemX);
                    setTranslationY(elemY);

//                    if (!Util.isLowAndroidVersion()) { // due to some lags on < 5.0
                    float rotation = ROTATION_ANGLE * 2 * (elemX - viewX) / getWidth();
                    if (touchPosition == TOUCH_BELOW) {
                        rotation = -rotation;
                    }
                    setRotation(rotation);
//                    } // seems that webview bug disappeared :thinking_face:

                    for (SwipeListener l : listeners) {
                        l.onScroll(elemX / screenWidth);
                    }
                }
                break;
        }
    }

    private void onActionUp(final float vx, final float vy) {
        final float x = getTranslationX();
//        final float y = getTranslationY();

        if (Math.abs(x) > MIN_FLING_TRANSLATION && Math.abs(vx) > MIN_FLING_VELOCITY && Math.abs(vx) > Math.abs(vy)
                || Math.abs(x) > MIN_SWIPE_TRANSLATION) {

            if (x > 0) {
                for (SwipeListener l : listeners) {
                    l.onSwipeRight();
                }
            } else {
                for (SwipeListener l : listeners) {
                    l.onSwipeLeft();
                }
            }
            AnimationHelper.createTransitionAnimation(this, Math.signum(x) * 2 * screenWidth, 0)
                    .rotation(0)
                    .setDuration(AnimationHelper.ANIMATION_DURATION * 2)
                    .withEndAction(() -> {
                        for (SwipeListener l : listeners) {
                            l.onSwiped();
                        }
                    });
        } else {
            if (Math.abs(vy) > MIN_FLING_VELOCITY) {
                for (SwipeListener l : listeners) {
                    l.onFlingDown();
                }
            }

            for (SwipeListener l : listeners) {
                l.onScroll(0);
            }
            AnimationHelper.playRollBackAnimation(this);
        }
    }

    public void swipeDown() {
        for (SwipeListener l : listeners) {
            l.onSwipeDown();
        }

        AnimationHelper.createTransitionAnimation(this, 0, screenHeight)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    for (SwipeListener l : listeners) {
                        l.onSwiped();
                    }
                });
    }

    public void setSwipeListener(@NonNull final SwipeListener listener) {
        this.listeners.add(listener);
    }

    public static class SwipeListener {
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
