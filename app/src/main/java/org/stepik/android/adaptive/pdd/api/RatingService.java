package org.stepik.android.adaptive.pdd.api;

import io.reactivex.Completable;
import io.reactivex.Observable;
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
    Observable<RatingResponse> getRating(
            @Query("course") final long course_id,
            @Query("count") final long count,
            @Query("days") final long days,
            @Query("user") final long profile_id
    );
}
