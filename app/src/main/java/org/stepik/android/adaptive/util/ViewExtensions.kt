package org.stepik.android.adaptive.util

import android.view.View

fun View.changeVisibillity(needShow: Boolean) {
    visibility = if (needShow) {
        View.VISIBLE
    } else {
        View.GONE
    }
}