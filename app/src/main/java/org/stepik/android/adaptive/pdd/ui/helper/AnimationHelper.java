package org.stepik.android.adaptive.pdd.ui.helper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;

public final class AnimationHelper {
    public static final long ANIMATION_DURATION = 200;

    public static OvershootInterpolator OvershootInterpolator2F = new OvershootInterpolator(1.5f);

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


    public static Animator.AnimatorListener onAnimationEnd(final Runnable runnable) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                runnable.run();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        };
    }
}
