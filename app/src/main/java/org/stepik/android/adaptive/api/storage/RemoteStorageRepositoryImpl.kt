package org.stepik.android.adaptive.api.storage

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepik.android.adaptive.api.storage.model.StorageRequest
import org.stepik.android.adaptive.api.storage.model.StorageResponse
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.model.QuestionsPackStorageItem
import org.stepik.android.adaptive.data.model.StorageRecord
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.util.toObject
import javax.inject.Inject

@AppSingleton
class RemoteStorageRepositoryImpl
@Inject
constructor(
    config: Config,
    private val remoteStorageService: RemoteStorageService,
    private val profilePreferences: ProfilePreferences
) : RemoteStorageRepository {
    private val packsKind = "adaptive_${config.courseId}_packs"
    private val gson = Gson()

    override fun storeQuestionsPack(packId: String): Completable =
        Single.fromCallable {
            gson.toJsonTree(QuestionsPackStorageItem(packId))
        }.flatMapCompletable { data ->
            remoteStorageService.createStorageRecord(
                StorageRequest(
                    StorageRecord(
                        kind = packsKind,
                        data = data
                    )
                )
            )
        }

    private fun getQuestionsPacks(page: Int): Observable<StorageResponse> =
        Observable.fromCallable(profilePreferences::profileId).flatMap {
            remoteStorageService.getStorageRecords(page, it, packsKind)
        }

    override fun getQuestionsPacks(): Single<List<String>> =
        getQuestionsPacks(page = 1).concatMap {
            if (it.meta?.hasNext == true) {
                Observable.just(it).concatWith(getQuestionsPacks(page = it.meta.page + 1))
            } else {
                Observable.just(it)
            }
        }.concatMap {
            it.records.map { it.data.toObject<QuestionsPackStorageItem>(gson).packId }.toObservable()
        }.toList()
}
