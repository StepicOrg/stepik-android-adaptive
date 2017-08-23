package org.stepik.android.adaptive.ui.helper;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;

public final class AnimationHelper {
    public static final long ANIMATION_DURATION = 200;

    private static final OvershootInterpolator OvershootInterpolator2F = new OvershootInterpolator(2f);

    public static void playWiggleAnimation(final View view) {
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 10);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(ANIMATION_DURATION / 2);
        animator.setRepeatCount(5);
        animator.start();
    }

    public static ViewPropertyAnimator createTransitionAnimation(final View view, final float x, final float y) {
        return view.animate().setStartDelay(0).setDuration(ANIMATION_DURATION)
                .translationX(x)
                .translationY(y);
    }

    public static ViewPropertyAnimator playRollBackAnimation(final View view) {
        return createTransitionAnimation(view, 0, 0)
                .rotation(0)
                .setInterpolator(OvershootInterpolator2F);
    }
}
