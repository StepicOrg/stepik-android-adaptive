package org.stepik.android.adaptive.pdd.data.model

import android.support.annotation.DrawableRes


data class Achievement(
        val title: String,
        val description: String,

        val path: String,
        val eventType: String,
        @DrawableRes val icon: Int
)