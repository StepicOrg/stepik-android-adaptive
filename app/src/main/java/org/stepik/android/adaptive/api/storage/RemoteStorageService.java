package org.stepik.android.adaptive.api.storage;

import org.stepik.android.adaptive.api.storage.model.StorageRequest;
import org.stepik.android.adaptive.api.storage.model.StorageResponse;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RemoteStorageService {

    @GET("api/storage-records")
    Observable<StorageResponse> getStorageRecords(
            @Query("page") final int page,
            @Query("user") final long userId,
            @Query("kind") final String kind
    );

    @POST("api/storage-records")
    Completable createStorageRecord(
            @Body final StorageRequest body
    );

    @PUT("api/storage-records/{recordId}")
    Completable setStorageRecord(
            @Path("recordId") final long recordId,
            @Body final StorageRequest body
    );

    @DELETE("api/storage-records/{recordId}")
    Completable removeStorageRecord(
            @Path("recordId") final long recordId
    );

}
