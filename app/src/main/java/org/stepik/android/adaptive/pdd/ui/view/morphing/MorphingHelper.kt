package org.stepik.android.adaptive.pdd.ui.view.morphing

import android.widget.TextView

object MorphingHelper {

    @JvmStatic
    fun morphStreakHeaderToIncBubble(header: MorphingView, inc: TextView) =
        MorphingAnimation(header, MorphingView.MorphParams(
                cornerRadius = 32f,

                marginRight = 32,

                width = inc.width,
                height = inc.height,

                textSize = inc.textSize,
                text = inc.text.toString()))

}