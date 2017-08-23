package org.stepik.android.adaptive.ui.animation

import android.content.Context
import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.github.jinatonic.confetti.CommonConfetti
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.ui.view.morphing.MorphingHelper
import org.stepik.android.adaptive.ui.view.morphing.MorphingView

object CardsFragmentAnimations {
    private val ANIMATION_START_DELAY_FOR_VIEWS_MS = 1500L
    private val ANIMATION_DURATION_MS = 200L
    private val FAST_ANIMATION_DURATION_MS = 100L

    @JvmStatic
    private var confettiColors: IntArray? = null

    @JvmStatic
    private fun initColors(context: Context) {
        if (confettiColors == null) {
            confettiColors = intArrayOf(
                    Color.BLACK,
                    ContextCompat.getColor(context, R.color.colorAccentDisabled),
                    ContextCompat.getColor(context, R.color.colorAccent)
            )
        }
    }

    @JvmStatic
    fun playStreakBubbleAnimation(greenStreakBubble: View) {
        greenStreakBubble.alpha = 1f
        greenStreakBubble.animate()
                .alpha(0f)
                .setInterpolator(DecelerateInterpolator())
                .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                .setDuration(ANIMATION_DURATION_MS)
                .start()
    }

    @JvmStatic
    fun playStreakRestoreAnimation(streakContainer: MorphingView) {
        streakContainer.morph(MorphingView.MorphParams(text = streakContainer.context.getString(R.string.streak_restored)))

        streakContainer.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_MS)
                .setStartDelay(0)
                .withEndAction {
                    streakContainer.animate()
                            .alpha(0f)
                            .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                            .setDuration(ANIMATION_DURATION_MS)
                            .start()
                }
                .start()
    }

    @JvmStatic
    fun playStreakSuccessAnimationSequence(binding: FragmentRecommendationsBinding) {
        binding.streakSuccessContainer.animate()
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction { playStreakMorphAnimation(binding) }
                .start()
    }


    private fun playStreakMorphAnimation(binding: FragmentRecommendationsBinding) {
        val params = binding.streakSuccessContainer.initialMorphParams

        MorphingHelper.morphStreakHeaderToIncBubble(binding.streakSuccessContainer, binding.expInc)
                .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                .withEndAction(Runnable {
                    binding.expProgress.visibility = View.VISIBLE
                    startConfettiExplosion(binding.root as CoordinatorLayout, binding.expBubble)

                    binding.streakSuccessContainer.animate()
                            .alpha(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                            .setDuration(ANIMATION_DURATION_MS)
                            .withEndAction { binding.streakSuccessContainer.morph(params) }
                            .start()
                })
                .setDuration(FAST_ANIMATION_DURATION_MS)
                .start()

        binding.expProgress.visibility = View.INVISIBLE
    }


    fun startConfettiExplosion(root: CoordinatorLayout, expBubble: View) {
        initColors(root.context)

        val x = (expBubble.x + (expBubble.parent as View).x).toInt() + expBubble.width / 2
        val y = (expBubble.y + expBubble.pivotY).toInt()
        CommonConfetti.explosion(root, x, y, confettiColors).oneShot()
    }

    @JvmStatic
    fun playStreakFailedAnimation(streakContainer: View, expProgress: View) {
        streakContainer.animate()
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction {
                    streakContainer.animate()
                            .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                            .setDuration(ANIMATION_DURATION_MS)
                            .alpha(0f)
                            .withEndAction { expProgress.visibility = View.VISIBLE }
                            .start()

                }.start()
        expProgress.visibility = View.INVISIBLE
    }
}