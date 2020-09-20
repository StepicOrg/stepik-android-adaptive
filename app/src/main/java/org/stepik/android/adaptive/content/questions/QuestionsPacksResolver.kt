package org.stepik.android.adaptive.content.questions

interface QuestionsPacksResolver {
    fun isAvailableForFree(pack: QuestionsPack): Boolean
    fun calcProgress(pack: QuestionsPack): Int
    fun getProgressDescription(pack: QuestionsPack): String
}
