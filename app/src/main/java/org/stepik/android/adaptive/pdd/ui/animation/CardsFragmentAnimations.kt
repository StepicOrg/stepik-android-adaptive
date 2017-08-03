package org.stepik.android.adaptive.pdd.ui.animation

import android.content.Context
import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.github.jinatonic.confetti.CommonConfetti
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.pdd.ui.view.morphing.MorphingHelper
import org.stepik.android.adaptive.pdd.ui.view.morphing.MorphingView

object CardsFragmentAnimations {
    private val VIEW_VISIBLE_MS = 1500L
    private val ANIMATION_SPEED_MS = 200L
    private val FAST_ANIMATION_SPEED_MS = 100L

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
    fun playStreakBubbleAnimation(expInc: View) {
        expInc.alpha = 1f
        expInc.animate()
                .alpha(0f)
                .setInterpolator(DecelerateInterpolator())
                .setStartDelay(VIEW_VISIBLE_MS)
                .setDuration(ANIMATION_SPEED_MS)
                .start()
    }

    @JvmStatic
    fun playStreakRestoreAnimation(streakContainer: MorphingView) {
        streakContainer.morph(MorphingView.MorphParams(text = streakContainer.context.getString(R.string.streak_restored)))

        streakContainer.animate()
                .alpha(1f)
                .setDuration(ANIMATION_SPEED_MS)
                .setStartDelay(0)
                .withEndAction {
                    streakContainer.animate()
                            .alpha(0f)
                            .setStartDelay(VIEW_VISIBLE_MS)
                            .setDuration(ANIMATION_SPEED_MS)
                            .start()
                }
                .start()
    }

    @JvmStatic
    fun playStreakSuccessAnimationSequence(binding: FragmentRecommendationsBinding) {
        binding.streakSuccessContainer.animate()
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(ANIMATION_SPEED_MS)
                .withEndAction { CardsFragmentAnimations.playStreakMorphAnimation(binding) }
                .start()
    }


    private fun playStreakMorphAnimation(binding: FragmentRecommendationsBinding) {
        val params = binding.streakSuccessContainer.initialMorphParams

        MorphingHelper.morphStreakHeaderToIncBubble(binding.streakSuccessContainer, binding.expInc)
                .setStartDelay(VIEW_VISIBLE_MS)
                .withEndAction(Runnable {
                    binding.expProgress.visibility = View.VISIBLE
                    confetti(binding.root as CoordinatorLayout, binding.expBubble)

                    binding.streakSuccessContainer.animate()
                            .alpha(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setStartDelay(VIEW_VISIBLE_MS)
                            .setDuration(ANIMATION_SPEED_MS)
                            .withEndAction { binding.streakSuccessContainer.morph(params) }
                            .start()
                })
                .setDuration(FAST_ANIMATION_SPEED_MS)
                .start()

        binding.expProgress.visibility = View.INVISIBLE
    }


    fun confetti(root: CoordinatorLayout, expBubble: View) {
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
                .setDuration(ANIMATION_SPEED_MS)
                .withEndAction {
                    streakContainer.animate()
                            .setStartDelay(VIEW_VISIBLE_MS)
                            .setDuration(ANIMATION_SPEED_MS)
                            .alpha(0f)
                            .withEndAction { expProgress.visibility = View.VISIBLE }
                            .start()

                }.start()
        expProgress.visibility = View.INVISIBLE
    }
}