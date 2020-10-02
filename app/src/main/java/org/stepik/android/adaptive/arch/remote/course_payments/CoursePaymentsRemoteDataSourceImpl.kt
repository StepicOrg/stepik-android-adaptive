package org.stepik.android.adaptive.arch.remote.course_payments

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.adaptive.api.StepikService
import org.stepik.android.adaptive.arch.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.adaptive.arch.domain.course_payments.model.CoursePayment
import org.stepik.android.adaptive.arch.remote.course_payments.model.CoursePaymentRequest
import org.stepik.android.adaptive.arch.remote.course_payments.model.CoursePaymentsResponse
import javax.inject.Inject

class CoursePaymentsRemoteDataSourceImpl
@Inject
constructor(
    private val stepikService: StepikService
) : CoursePaymentsRemoteDataSource {

    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment> =
        stepikService
            .createCoursePayment(
                CoursePaymentRequest(
                    CoursePaymentRequest.Body(
                        course   = courseId,
                        provider = CoursePaymentRequest.Body.Provider.GOOGLE,
                        data     = CoursePaymentRequest.Body.Data(
                            token       = purchase.token,
                            packageName = purchase.packageName,
                            productId   = purchase.sku,
                            amount      = sku.detailedPrice.amount / 1_000_000f,
                            currency    = sku.detailedPrice.currency
                        )
                    )
                )
            )
            .map { it.coursePayments.first() }

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?): Single<List<CoursePayment>> =
        stepikService
            .getCoursePaymentsByCourseId(courseId)
            .map(CoursePaymentsResponse::coursePayments)
            .map { payments ->
                if (coursePaymentStatus != null) {
                    payments.filter { it.status == coursePaymentStatus }
                } else {
                    payments
                }
            }
}
