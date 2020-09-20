package org.stepik.android.adaptive.api.rating

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.data.model.RatingItem

interface RatingRepository {
    fun fetchRating(): Single<Long>
    fun getRatingTable(count: Int, days: Int): Single<List<RatingItem>>
    fun putRating(exp: Long): Completable
}
