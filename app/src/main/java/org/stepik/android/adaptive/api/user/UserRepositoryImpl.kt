package org.stepik.android.adaptive.api.user

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.adaptive.data.model.User
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class UserRepositoryImpl
@Inject
constructor(
        private val userService: UserService
): UserRepository {

    override fun getUsers(ids: LongArray): Single<List<User>> =
            userService.getUsers(ids, 1).concatMap {
                if (it.meta.hasNext) {
                    Observable.just(it).concatWith(userService.getUsers(ids, it.meta.page + 1))
                } else {
                    Observable.just(it)
                }
            }.concatMap {
                it.users.toObservable()
            }.toList()

}