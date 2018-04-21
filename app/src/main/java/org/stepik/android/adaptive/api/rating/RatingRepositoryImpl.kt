package org.stepik.android.adaptive.api.rating

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.adaptive.api.rating.model.RatingRequest
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.model.RatingItem
import org.stepik.android.adaptive.data.preference.AuthPreferences
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class RatingRepositoryImpl
@Inject
constructor(
        private val config: Config,
        private val ratingService: RatingService,
        private val authPreferences: AuthPreferences,
        private val profilePreferences: ProfilePreferences
): RatingRepository {
    override fun fetchRating(): Single<Long> =
            ratingService.restoreRating(config.courseId, authPreferences.oAuthResponse?.accessToken).map { it.exp }

    override fun getRatingTable(count: Int, days: Int): Single<List<RatingItem>> =
            ratingService.getRating(config.courseId, count.toLong(), days.toLong(), profilePreferences.profileId).map { it.users }

    override fun putRating(exp: Long): Completable =
            ratingService.putRating(RatingRequest(exp, config.courseId, authPreferences.oAuthResponse?.accessToken))
}