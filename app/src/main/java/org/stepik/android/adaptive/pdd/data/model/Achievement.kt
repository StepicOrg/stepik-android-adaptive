package org.stepik.android.adaptive.pdd.data.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

data class Achievement(val id: Int,
                       @DrawableRes val iconId: Int,
                       @StringRes val titleId: Int,
                       @StringRes val descriptionId: Int,
                       var completed: Boolean)