package org.stepik.android.adaptive.api.user

import io.reactivex.Observable
import org.stepik.android.adaptive.api.user.model.UsersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {

    @GET("api/users")
    fun getUsers(
            @Query("ids[]") userIds: LongArray,
            @Query("page") page: Int
    ): Observable<UsersResponse>

}