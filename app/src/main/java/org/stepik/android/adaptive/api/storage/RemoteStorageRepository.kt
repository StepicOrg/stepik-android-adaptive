package org.stepik.android.adaptive.api.storage

import io.reactivex.Completable
import io.reactivex.Single

interface RemoteStorageRepository {
    fun storeQuestionsPack(packId: String): Completable
    fun getQuestionsPacks(): Single<List<String>>
}