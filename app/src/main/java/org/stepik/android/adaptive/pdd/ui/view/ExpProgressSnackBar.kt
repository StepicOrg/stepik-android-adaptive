package org.stepik.android.adaptive.pdd.ui.view

import android.databinding.DataBindingUtil
import android.support.design.widget.BaseTransientBottomBar
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.ExpProgressSnackBarBinding


class ExpProgressSnackBar(parent: ViewGroup, content: View, callback: ContentViewCallback) : BaseTransientBottomBar<ExpProgressSnackBar>(parent, content, callback)  {

    companion object {
        fun make(parent: ViewGroup, exp: Long, level: Long, nextLevel: Long) : ExpProgressSnackBar {
            val binding = DataBindingUtil.inflate<ExpProgressSnackBarBinding>(LayoutInflater.from(parent.context), R.layout.exp_progress_snack_bar, parent, false)
            val callback = ContentViewCallback(binding.root)

            binding.expProgress.max = 100

            val percents = (100 * (exp.toDouble() / nextLevel)).toInt()

            binding.expTitle.text = String.format(parent.context.getString(R.string.exp_title), level)

            binding.expProgress.progress = percents
            binding.expPercents.text = "$percents%"
            binding.expTotal.text = "$exp/$nextLevel"

            return ExpProgressSnackBar(parent, binding.root, callback).setDuration(LENGTH_SHORT)
        }
    }

    class ContentViewCallback(private val content: View) : BaseTransientBottomBar.ContentViewCallback {
        override fun animateContentOut(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 1f)
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration.toLong())
                    .startDelay = delay.toLong()
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 0f)
            ViewCompat.animate(content)
                    .scaleY(1f)
                    .setDuration(duration.toLong())
                    .startDelay = delay.toLong()
        }

    }
}
