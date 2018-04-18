package org.stepik.android.adaptive.api.user

import io.reactivex.Single
import org.stepik.android.adaptive.data.model.User

interface UserRepository {
    fun getUsers(ids: LongArray): Single<List<User>>
}