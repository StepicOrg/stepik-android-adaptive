package org.stepik.android.adaptive.arch.view.injection.course_payments

import dagger.Binds
import dagger.Module
import org.stepik.android.adaptive.arch.data.course_payments.repository.CoursePaymentsRepositoryImpl
import org.stepik.android.adaptive.arch.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.adaptive.arch.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.adaptive.arch.remote.course_payments.CoursePaymentsRemoteDataSourceImpl

@Module
abstract class CoursePaymentsDataModule {
    @Binds
    internal abstract fun bindCoursePaymentsRepository(
        coursePaymentsRepositoryImpl: CoursePaymentsRepositoryImpl
    ): CoursePaymentsRepository

    @Binds
    internal abstract fun bindCoursePaymentsRemoteDataSource(
        coursePaymentsRemoteDataSource: CoursePaymentsRemoteDataSourceImpl
    ): CoursePaymentsRemoteDataSource
}
