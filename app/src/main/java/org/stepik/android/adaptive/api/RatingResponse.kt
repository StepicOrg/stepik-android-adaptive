package org.stepik.android.adaptive.api

import org.stepik.android.adaptive.data.model.RatingItem

data class RatingResponse (@JvmField val count: Long,
                           @JvmField val users: List<RatingItem>)