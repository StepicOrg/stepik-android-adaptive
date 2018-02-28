package org.stepik.android.adaptive.ui.helper

import android.content.res.Resources
import android.support.annotation.ColorInt

@ColorInt
fun setAlpha(@ColorInt color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

fun dpToPx(dp: Int) = Resources.getSystem().displayMetrics.density * dp