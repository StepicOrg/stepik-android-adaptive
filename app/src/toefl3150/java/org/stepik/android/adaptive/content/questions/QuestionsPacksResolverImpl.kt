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
    override fun isAvailableForFree(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Basic      -> true
        QuestionsPack.Adjectives -> expManager.getCurrentLevel(expManager.exp) >= 10
        QuestionsPack.Verbs      -> expManager.getCurrentLevel(expManager.exp) >= 12
        else                     -> false
    }

    override fun calcProgress(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Adjectives -> min(expManager.exp * 100 / expManager.getNextLevelExp(10 - 1L), 100).toInt()
        QuestionsPack.Verbs      -> min(expManager.exp * 100 / expManager.getNextLevelExp(12 - 1L), 100).toInt()
        else -> 0
    }
}