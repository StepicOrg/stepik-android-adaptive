package org.stepik.android.adaptive.content.questions

import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class QuestionsPacksManager
@Inject
constructor(
    private val analytics: Analytics,
    private val sharedPreferenceHelper: SharedPreferenceHelper,

    val questionsPacks: Array<QuestionsPack>
) {
    private val idToPack = questionsPacks.associateBy { it.id }

    val ids = idToPack.keys.toList()

    val currentPackIndex: Int
        get() {
            val index = sharedPreferenceHelper.questionsPackIndex
            return if (index in 0 until questionsPacks.size) {
                index
            } else {
                0
            }
        }

    val currentPack: QuestionsPack
        get() = questionsPacks[currentPackIndex]

    val currentCourseId: Long
        get() = currentPack.courseId

    val isQuestionsPacksSupported: Boolean =
        questionsPacks.size > 1

    fun onQuestionsPackViewed(pack: QuestionsPack) {
        sharedPreferenceHelper.onQuestionsPackViewed(pack)
    }

    val unviewedPacksCount: Int
        get() = questionsPacks.count { !sharedPreferenceHelper.isQuestionsPackViewed(it) }

    fun getPackById(id: String): QuestionsPack? =
        idToPack[id]

    fun switchPack(pack: QuestionsPack) {
        sharedPreferenceHelper.questionsPackIndex = pack.ordinal
        analytics.logEventWithLongParam(Analytics.EVENT_ON_QUESTIONS_PACK_SWITCHED, Analytics.PARAM_COURSE, pack.courseId)
    }
}
