package org.stepik.android.adaptive.ui.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.model.Achievement
import org.stepik.android.adaptive.databinding.PopupAchievementBinding
import org.stepik.android.adaptive.ui.view.morphing.MorphingAnimation
import org.stepik.android.adaptive.ui.view.morphing.MorphingView

object AchievementAnimations {
    private const val ANIMATION_IN_DURATION = 400L
    private const val ANIMATION_OUT_DURATION = 400L
    private const val ANIMATION_OUT_START_DELAY = 200L

    private const val MORPHING_ANIMATION_DURATION = 400L
    private const val MORPHING_ANIMATION_START_DELAY = 100L

    private const val TEXT_ANIMATION_DURATION = 200L
    private const val HIDE_ANIMATION_START_DELAY = 2000L

    @JvmStatic
    fun show(container: ViewGroup, achievement: Achievement): ChainedAnimator {
        val context = container.context
        val binding = PopupAchievementBinding.inflate(LayoutInflater.from(context), container, false)

        val bg = GradientDrawable()
        bg.setColor(ContextCompat.getColor(context, R.color.colorAccent))
        bg.cornerRadius = 4f

        bg.setStroke(2, ContextCompat.getColor(context, R.color.colorAccentDarker))


        binding.root.background = bg

        binding.morphing.nestedTextView = binding.title

        binding.root.scaleX = 0f
        binding.root.scaleY = 0f

        binding.root.setOnClickListener { ScreenManager.showStatsScreen(context, 1) }

        container.addView(binding.root)

        return ChainedAnimator { inAnimation(binding) }
                .then { showText(binding, context.getString(R.string.achievement_unlocked)) }
                .then(hideText(binding))
                .then { showText(binding, achievement.title) }
                .then(hideText(binding))
                .then { showText(binding, achievement.description) }
                .then(hideText(binding))
                .then { showText(binding, context.getString(R.string.achievement_tap_for_more)) }
                .then(hideText(binding))
                .then(hide(container, binding))
                .start()
    }

    private fun inAnimation(binding: PopupAchievementBinding) : Animator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(binding.root, "scaleX", 1f),
                ObjectAnimator.ofFloat(binding.root, "scaleY", 1f)
        )
        set.duration = ANIMATION_IN_DURATION
        set.interpolator = OvershootInterpolator(3f)

        return set
    }


    private fun showText(binding: PopupAchievementBinding, text: String) : Animator {
        val set = AnimatorSet()

        binding.title.text = text
        binding.title.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val width = Math.min(binding.title.measuredWidth, binding.root.context.resources.displayMetrics.widthPixels - (16 + 16 + 32 + 24) * 2)
        val height = binding.title.measuredHeight

        val morph = MorphingAnimation(binding.morphing, MorphingView.MorphParams(width = width), OvershootInterpolator(2f))
                .setDuration(MORPHING_ANIMATION_DURATION)
                .setStartDelay(MORPHING_ANIMATION_START_DELAY)
                .getAnimator()

        binding.title.alpha = 0f
        binding.title.translationY = height.div(2).toFloat()

        set.playSequentially(morph, textInAnimator(binding))

        return set
    }

    private fun textInAnimator(binding: PopupAchievementBinding): Animator {
        val set = AnimatorSet()

        set.playTogether(
                ObjectAnimator.ofFloat(binding.title, "alpha", 1f),
                ObjectAnimator.ofFloat(binding.title, "translationY", 0f)
        )
        set.duration = TEXT_ANIMATION_DURATION
        set.startDelay = MORPHING_ANIMATION_START_DELAY

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.title.visibility = View.VISIBLE
            }
        })

        return set
    }

    @JvmStatic
    private fun hideText(binding: PopupAchievementBinding) = ChainedAnimator {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(binding.title, "alpha", 0f),
                ObjectAnimator.ofFloat(binding.title, "translationY", -binding.title.height / 2f)
        )

        set.duration = TEXT_ANIMATION_DURATION
        set.startDelay = HIDE_ANIMATION_START_DELAY

        set
    }.withEndAction {
        binding.title.visibility = View.GONE
    }

    private fun outAnimation(container: ViewGroup, binding: PopupAchievementBinding) = ChainedAnimator {
        val set = AnimatorSet()

        set.playTogether(
                ObjectAnimator.ofFloat(binding.root, "scaleX", 0f),
                ObjectAnimator.ofFloat(binding.root, "scaleY", 0f)
        )

        set.interpolator = AnticipateInterpolator()
        set.duration = ANIMATION_OUT_DURATION
        set.startDelay = ANIMATION_OUT_START_DELAY

        set
    }.withEndAction { container.removeView(binding.root) }

    @JvmStatic
    private fun hide(container: ViewGroup, binding: PopupAchievementBinding) : ChainedAnimator {
        val morph = ChainedAnimator {
            MorphingAnimation(binding.morphing, MorphingView.MorphParams(width = 0), AccelerateDecelerateInterpolator())
                    .setDuration(MORPHING_ANIMATION_DURATION)
                    .setStartDelay(MORPHING_ANIMATION_START_DELAY)
                    .getAnimator()
        }

        morph.then(outAnimation(container, binding))
        return morph
    }
}