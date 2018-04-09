package org.stepik.android.adaptive.api.storage

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.stepik.android.adaptive.api.StepikService
import org.stepik.android.adaptive.api.storage.model.StorageRequest
import org.stepik.android.adaptive.api.storage.model.StorageResponse
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.SharedPreferenceHelper
import org.stepik.android.adaptive.data.model.QuestionsPackStorageItem
import org.stepik.android.adaptive.data.model.StorageRecord
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class StorageRepositoryImpl
@Inject
constructor(
        config: Config,
        private val stepikService: StepikService,
        private val sharedPreferenceHelper: SharedPreferenceHelper
): StorageRepository {
    private val packsKind = "adaptive_${config.courseId}_packs"

    override fun storeQuestionsPack(packId: String): Completable =
            stepikService.createStorageRecord(StorageRequest(StorageRecord(
                    kind = packsKind,
                    data = QuestionsPackStorageItem(packId)
            )))

    private fun getQuestionsPacks(page: Int = 0): Observable<StorageResponse<QuestionsPackStorageItem>> =
            Observable.fromCallable(sharedPreferenceHelper::profileId).flatMap {
                stepikService.getStorageRecords<QuestionsPackStorageItem>(page, it, packsKind)
            }


    override fun getQuestionsPacks(): Single<List<String>> = getQuestionsPacks(page = 0).concatMap {
        if (it.meta?.hasNext == true) {
            Observable.just(it).concatWith(getQuestionsPacks(page = it.meta.page))
        } else {
            Observable.just(it)
        }
    }.concatMap {
        Observable.fromIterable(it.records.map { it.data.packId })
    }.toList()
}