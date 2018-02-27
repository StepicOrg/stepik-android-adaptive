package org.stepik.android.adaptive.ui.helper

import android.support.annotation.ColorInt

@ColorInt
fun setAlpha(@ColorInt color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)