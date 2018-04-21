package org.stepik.android.adaptive.di.study

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.resolvers.StepTypeResolver
import org.stepik.android.adaptive.resolvers.StepTypeResolverImpl

@Module
interface StudyModule {

    @Binds
    @StudyScope
    fun provideStepTypeResolver(stepTypeResolverImpl: StepTypeResolverImpl): StepTypeResolver

}