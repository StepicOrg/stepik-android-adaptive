package org.stepik.droid.adaptive.pdd.ui.helper;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

public final class AnimationHelper {
    public static final long ANIMATION_DURATION = 450;

    public static OvershootInterpolator OvershootInterpolator2F = new OvershootInterpolator(2f);


    public static ValueAnimator.AnimatorUpdateListener createLayoutMarginAnimation(final RelativeLayout layout) {
        return (animation) -> LayoutHelper.setRelativeLayoutMarginTop(layout,
                (Integer) animation.getAnimatedValue());
    }

    public static void playWiggleAnimation(final View view) {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 10);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(100);
        animator.setRepeatCount(5);
        animator.start();
    }

    public static ViewPropertyAnimator createTransitionAnimation(final View view, final float x, final float y) {
        return view.animate().setStartDelay(0).setDuration(ANIMATION_DURATION)
                .translationX(x)
                .translationY(y);
    }

    public static void playRollBackAnimation(final View view) {
        createTransitionAnimation(view, 0, 0).setInterpolator(OvershootInterpolator2F).start();
    }

    public static ViewPropertyAnimator createReactionAppearAnimation(final View view) {
        return view.animate()
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION);
    }

    public static ViewPropertyAnimator createReactionDisappearAnimation(final View view) {
        return view.animate()
                .scaleX(0)
                .scaleY(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION);
    }
}
