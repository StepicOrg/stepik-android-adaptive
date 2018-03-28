package org.stepik.android.adaptive.di.content.questions

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.content.questions.packs.QuestionsPacksList
import org.stepik.android.adaptive.content.questions.packs.QuestionsPacksListImpl
import org.stepik.android.adaptive.di.AppSingleton

@Module
abstract class QuestionsModule {

    @AppSingleton
    @Binds
    internal abstract fun provideQuestionsPacksList(questionsPacksList: QuestionsPacksListImpl): QuestionsPacksList

}