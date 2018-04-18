package org.stepik.android.adaptive.api.storage

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import org.stepik.android.adaptive.di.network.NetworkHelper

class RemoteStorageRepositoryTest {
    @Test
    fun getQuestionsPacksPaginationTest() {
        val server = MockWebServer()

        val base = server.url("/").toString()

        val courseId = 111L
        val config = mock<Config>()
        whenever(config.courseId) doReturn courseId

        val profileId = 333L
        val profilePreferences = mock<ProfilePreferences>()
        whenever(profilePreferences.profileId) doReturn profileId

        val okHttpClient = OkHttpClient.Builder().build()
        val remoteStorageService = NetworkHelper.createRetrofit(okHttpClient, base).create(RemoteStorageService::class.java)

        val packsIds = listOf("pro", "full")
        packsIds.forEachIndexed { i, packId ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""
                {
                    "meta": {
                        "page": ${i + 1},
                        "has_next": ${i + 1 < packsIds.size},
                        "has_previous": false
                    },
                    "storage-records": [
                        {
                            "id": 58,
                            "user": $profileId,
                            "kind": "adaptive_${courseId}_packs",
                            "data": {
                                "pack_id": "$packId"
                            },
                            "create_date": "2018-04-10T10:10:58.644Z",
                            "update_date": "2018-04-10T10:10:58.644Z"
                        }
                    ]
                }
            """.trimIndent()))
        }


        val remoteStorageRepository: RemoteStorageRepository = RemoteStorageRepositoryImpl(config, remoteStorageService, profilePreferences)

        remoteStorageRepository.getQuestionsPacks()
                .test()
                .assertResult(packsIds)
                .assertComplete()
    }
}