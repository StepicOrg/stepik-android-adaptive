package org.stepik.android.adaptive.api.rating;

import org.stepik.android.adaptive.api.rating.model.RatingRequest;
import org.stepik.android.adaptive.api.rating.model.RatingResponse;
import org.stepik.android.adaptive.api.rating.model.RatingRestoreResponse;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface RatingService {

    @PUT("rating")
    Completable putRating(
        @Body final RatingRequest ratingRequest
    );

    @GET("rating")
    Single<RatingResponse> getRating(
            @Query("course") final long courseId,
            @Query("count") final long count,
            @Query("days") final long days,
            @Query("user") final long profileId
    );

    @GET("rating-restore")
    Single<RatingRestoreResponse> restoreRating(
            @Query("course") final long courseId,
            @Query("token") final String token
    );
}
