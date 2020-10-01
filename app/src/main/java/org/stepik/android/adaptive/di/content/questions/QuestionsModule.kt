package org.stepik.android.adaptive.di.content.questions

import dagger.Module
import dagger.Provides
import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class QuestionsModule {

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideQuestionsPacksList(): Array<QuestionsPack> =
            QuestionsPack.values()
    }
}
