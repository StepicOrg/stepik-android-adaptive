package org.stepik.android.adaptive.content.questions

import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.ExpManager
import javax.inject.Inject
import kotlin.math.min

@AppSingleton
class QuestionsPacksResolverImpl
@Inject
constructor(
        private val expManager: ExpManager
): QuestionsPacksResolver {
    companion object {
        private const val ADJECTIVES_PACK_TARGET_LEVEL = 10
        private const val VERBS_PACK_TARGET_LEVEL = 12
    }

    override fun isAvailableForFree(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Basic      -> true
        QuestionsPack.Adjectives -> expManager.getCurrentLevel(expManager.exp) >= ADJECTIVES_PACK_TARGET_LEVEL
        QuestionsPack.Verbs      -> expManager.getCurrentLevel(expManager.exp) >= VERBS_PACK_TARGET_LEVEL
        else                     -> false
    }

    override fun calcProgress(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Adjectives -> min(expManager.exp * 100 / expManager.getNextLevelExp(ADJECTIVES_PACK_TARGET_LEVEL - 1L), 100).toInt()
        QuestionsPack.Verbs      -> min(expManager.exp * 100 / expManager.getNextLevelExp(VERBS_PACK_TARGET_LEVEL - 1L), 100).toInt()
        else -> 0
    }
}