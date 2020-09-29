package org.stepik.android.adaptive.arch.data.course_payments.repository

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.arch.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.adaptive.arch.domain.base.DataSourceType
import org.stepik.android.adaptive.arch.domain.course_payments.model.CoursePayment
import org.stepik.android.adaptive.arch.domain.course_payments.repository.CoursePaymentsRepository
import javax.inject.Inject

class CoursePaymentsRepositoryImpl
@Inject
constructor(
    private val coursePaymentsRemoteDataSource: CoursePaymentsRemoteDataSource
) : CoursePaymentsRepository {
    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment> =
        coursePaymentsRemoteDataSource
            .createCoursePayment(courseId, sku, purchase)

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?, sourceType: DataSourceType): Single<List<CoursePayment>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                coursePaymentsRemoteDataSource
                    .getCoursePaymentsByCourseId(courseId, coursePaymentStatus)

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
}
