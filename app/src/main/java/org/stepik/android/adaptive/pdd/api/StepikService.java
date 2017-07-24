package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.EnrollmentWrapper;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface StepikService {

@Headers("Content-Type: application/json")
@POST("api/enrollments")
Completable joinCourse(
        @Body EnrollmentWrapper enrollmentCourse
);


@GET("api/recommendations")
Observable<RecommendationsResponse> getNextRecommendations(
        @Query("course") final long course_id,
        @Query("count") final int count
);


@GET("api/steps")
Observable<StepsResponse> getSteps(
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
Completable createSubmission(
     @Body final SubmissionRequest submissionRequest
);

@GET("api/submissions")
Observable<SubmissionResponse> getSubmissions(
        @Query("attempt") final long attempt_id,
        @Query("order") final String desc
);

@GET("api/lessons/{lesson}")
Observable<LessonsResponse> getLessons(
        @Path("lesson") final long lesson
);

@GET("api/stepics/1")
Observable<ProfileResponse> getProfile();

@POST("api/recommendation-reactions")
Completable createRecommendationReaction(
        @Body final RecommendationReactionsRequest reactionsRequest
);


@GET("api/units")
Observable<UnitsResponse> getUnits(
        @Query("course") final long course,
        @Query("lesson") final long lesson
);

@POST("api/views")
Completable reportView(
        @Body final ViewRequest viewRequest
);


@PUT("api/profiles/{userId}")
Completable setProfile(
        @Path("userId") final long userId,
        @Body final ProfileRequest profileRequest
);

}
