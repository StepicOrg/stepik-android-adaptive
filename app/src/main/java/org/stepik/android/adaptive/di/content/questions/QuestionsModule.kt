package org.stepik.android.adaptive.di.content.questions

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.content.questions.QuestionsPacksResolver
import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.content.questions.QuestionsPacksResolverImpl
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class QuestionsModule {

    @Binds
    @AppSingleton
    abstract fun provideQuestionsPacksResolver(questionsPacksResolverImpl: QuestionsPacksResolverImpl): QuestionsPacksResolver


    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideQuestionsPacksList(): Array<QuestionsPack> = QuestionsPack.values()
    }

}