package org.stepik.android.adaptive.pdd.data.model

data class WeekProgress(val start: Long, val end: Long, val values: List<Long>) {
    val total = values.sum()
}