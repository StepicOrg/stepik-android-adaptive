package org.stepik.android.adaptive.content.questions

import org.stepik.android.adaptive.content.questions.packs.QuestionsPack
import org.stepik.android.adaptive.content.questions.packs.QuestionsPacksList
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class QuestionsPacksManager
@Inject
constructor(
        private val analytics: Analytics,
        private val sharedPreferenceMgr: SharedPreferenceMgr,

        questionsPacksList: QuestionsPacksList
) {
    val questionsPacks = questionsPacksList.questionsPacks

    private val idToPack = questionsPacks.associateBy { it.id }

    val ids = idToPack.keys.toList()

    val currentPackIndex: Int
        get() {
            val index = sharedPreferenceMgr.questionsPackIndex
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

    val isQuestionsPacksSupported = questionsPacks.size > 1

    fun onQuestionsPackViewed(pack: QuestionsPack) =
        sharedPreferenceMgr.onQuestionsPackViewed(pack)

    val unviewedPacksCount: Int
        get() = questionsPacks.count { !sharedPreferenceMgr.isQuestionsPackViewed(it) }

    fun getPackById(id: String) = idToPack[id]

    fun switchPack(pack: QuestionsPack) {
        sharedPreferenceMgr.questionsPackIndex = pack.ordinal
        analytics.logEventWithLongParam(Analytics.EVENT_ON_QUESTIONS_PACK_SWITCHED, Analytics.PARAM_COURSE, pack.courseId)
    }
}