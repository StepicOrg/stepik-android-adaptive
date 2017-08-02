package org.stepik.android.adaptive.pdd.ui.view.morphing

import android.widget.TextView
import org.stepik.android.adaptive.pdd.R

object MorphingHelper {

    @JvmStatic
    fun morphStreakHeaderToIncBubble(header: MorphingView, inc: TextView) =
        MorphingAnimation(header, MorphingView.MorphParams(
                cornerRadius = header.context.resources.getDimension(R.dimen.exp_bubble_corner_radius),

                marginRight = header.context.resources.getDimension(R.dimen.exp_bubble_margin).toInt(),

                width = inc.width,
                height = inc.height,

                textSize = inc.textSize,
                text = inc.text.toString()))

}