package org.stepik.android.adaptive.util

import android.os.Build
import android.text.Html
import android.text.Spanned

fun fromHtmlCompat(html: String): Spanned = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
    @Suppress("DEPRECATION")
    Html.fromHtml(html)
} else {
    Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
}