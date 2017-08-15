package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.RatingItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RatingService {

    @POST("submissions")
    Completable createSubmission(
            @Body final SubmissionRequest submissionRequest
    );

    @GET("submissions")
    Observable<SubmissionResponse> getSubmissions(
            @Query("attempt") final long attempt_id,
            @Query("order") final String desc
    );

    @GET("rating")
    Observable<List<RatingItem>> getRating(
            @Query("course") final long course_id,
            @Query("count") final long count,
            @Query("days") final long days
    );
}
