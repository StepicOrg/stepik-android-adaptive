package org.stepik.android.adaptive.content.questions

import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class QuestionsPacksResolverImpl
@Inject
constructor(): QuestionsPacksResolver {
    override fun isAvailableForFree(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Basic      -> true
        else                     -> false
    }

    override fun calcProgress(pack: QuestionsPack) = 0
    override fun getProgressDescription(pack: QuestionsPack) = String()
}