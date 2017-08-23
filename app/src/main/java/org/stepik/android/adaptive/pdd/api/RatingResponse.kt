package org.stepik.android.adaptive.pdd.api

import org.stepik.android.adaptive.pdd.data.model.RatingItem

data class RatingResponse (@JvmField val count: Long,
                           @JvmField val users: List<RatingItem>)