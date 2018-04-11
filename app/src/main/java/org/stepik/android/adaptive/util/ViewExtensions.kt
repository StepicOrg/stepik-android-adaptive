package org.stepik.android.adaptive.util

import android.view.View
import android.view.ViewGroup

fun View.changeVisibillity(needShow: Boolean) {
    visibility = if (needShow) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun ViewGroup.hideAllChildren() {
    for (i in 0 until childCount) {
        getChildAt(i).changeVisibillity(false)
    }
}