package org.stepik.droid.adaptive.pdd.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StepikService {

@GET("api/recommendations")
Call<RecommendationsResponse> getNextRecommendations(
        @Query("course") final long course_id
);


@GET("api/steps")
Call<StepsResponse> getSteps(
        @Query("lesson") final long lesson_id
);

@POST("api/attempts")
Observable<AttemptResponse> createAttempt(
        @Body final AttemptRequest attemptRequest
);

@GET("api/attempts")
Observable<AttemptResponse> getAttempts(
    @Query("step") final long step,
    @Query("user") final long user
);

@POST("api/submissions")
Observable<SubmissionResponse> createSubmission(
     @Body final SubmissionRequest submissionRequest
);

@GET("api/submissions")
Observable<SubmissionResponse> getSubmissions(
        @Query("attempt") final long attempt_id,
        @Query("order") final String desc
);

@GET("api/stepics/1")
Observable<ProfileResponse> getProfile();

@POST("api/recommendation-reactions")
Call<RecommendationReactionsResponse> createRecommendationReaction(
        @Body final RecommendationReactionsRequest reactionsRequest
);

}
