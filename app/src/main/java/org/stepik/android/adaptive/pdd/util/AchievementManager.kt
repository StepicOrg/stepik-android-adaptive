package org.stepik.android.adaptive.pdd.util

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.PopupAchievementBinding
import org.stepik.android.adaptive.pdd.ui.view.morphing.MorphingAnimation
import org.stepik.android.adaptive.pdd.ui.view.morphing.MorphingView

object AchievementManager {

    fun show(container: ViewGroup) {
        val context = container.context
        val binding = PopupAchievementBinding.inflate(LayoutInflater.from(context), container, false)

        val bg = GradientDrawable()
        bg.setColor(ContextCompat.getColor(context, R.color.colorAccent))
        bg.cornerRadius = 4f


        binding.root.background = bg

        binding.morphing.nestedTextView = binding.title

        binding.root.scaleX = 0f
        binding.root.scaleY = 0f

        container.addView(binding.root)

        binding.root.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(OvershootInterpolator(3f))
                .withEndAction {
                    showText(binding, context.getString(R.string.achievement_unlocked)) {
                        hideText(binding) {
                            showText(binding, "Знаток\nДостигнуть 5 уровня") {
                                hideText(binding) {
                                    hide(binding)
                                }
                            }
                        }
                    }
                }
                .setDuration(400).start()
    }

    private fun showText(binding: PopupAchievementBinding, text: String, then: () -> Unit) {
        binding.title.text = text
        binding.title.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val width = binding.title.measuredWidth
        val height = binding.title.measuredHeight

        MorphingAnimation(binding.morphing, MorphingView.MorphParams(width = width), OvershootInterpolator(2f)).setDuration(400).setStartDelay(200).withEndAction(Runnable {
            binding.title.alpha = 0f
            binding.title.translationY = height.div(2).toFloat()
            binding.title.visibility = View.VISIBLE

            binding.title.animate()
                    .alpha(1f)
                    .setStartDelay(200)
                    .setDuration(200)
                    .translationY(0f)
                    .withEndAction(then)
                    .start()
        }).start()
    }

    private fun hideText(binding: PopupAchievementBinding, then: () -> Unit) {
        binding.title.animate()
                .alpha(0f)
                .setStartDelay(400)
                .setDuration(200)
                .translationY(-binding.title.height / 2f)
                .withEndAction {
                    binding.title.visibility = View.GONE
                    then()
                }
                .start()
    }

    private fun hide(binding: PopupAchievementBinding) {
        MorphingAnimation(binding.morphing, MorphingView.MorphParams(width = 0), AccelerateDecelerateInterpolator()).setDuration(200).setStartDelay(200).withEndAction(Runnable {
            binding.root.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setStartDelay(400)
                    .setDuration(400).start()
        }).start()
    }

}