package org.stepik.android.adaptive.arch.domain.question_packs.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.solovyev.android.checkout.ProductTypes
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.arch.domain.base.DataSourceType
import org.stepik.android.adaptive.arch.domain.billing.model.SkuSerializableWrapper
import org.stepik.android.adaptive.arch.domain.billing.repository.BillingRepository
import org.stepik.android.adaptive.arch.domain.course_payments.model.CoursePayment
import org.stepik.android.adaptive.arch.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.adaptive.arch.domain.question_packs.model.EnrollmentState
import org.stepik.android.adaptive.arch.domain.question_packs.model.QuestionListItem
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.data.model.Course
import javax.inject.Inject

class QuestionPacksInteractor
@Inject
constructor(
    private val api: Api,
    private val billingRepository: BillingRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val questionsPacksManager: QuestionsPacksManager
) {
    companion object {
        private const val COURSE_TIER_PREFIX = "course_tier_"
    }

    fun getQuestionListItems(vararg courseId: Long): Single<List<QuestionListItem>> =
        api.getCourses(courseId)
            .flatMap { obtainQuestionListItem(it.courses) }

    private fun obtainQuestionListItem(courses: List<Course>): Single<List<QuestionListItem>> =
        resolveCoursesEnrollmentStates(courses).map { enrollmentStates ->
            val enrollmentMap = enrollmentStates.toMap()

            courses.map { course ->
                QuestionListItem(
                    course = course,
                    questionPack = questionsPacksManager.getPackByCourseId(course.id) ?: throw IllegalArgumentException(),
                    enrollmentState = enrollmentMap.getValue(course.id)
                )
            }
        }

    private fun resolveCoursesEnrollmentStates(courses: List<Course>): Single<List<Pair<Long, EnrollmentState>>> =
        courses
            .toObservable()
            .flatMapSingle { resolveCourseEnrollmentState(it) }
            .toList()

    private fun resolveCourseEnrollmentState(course: Course): Single<Pair<Long, EnrollmentState>> =
        when {
            course.enrollment > 0L ->
                Single.just(course.id to EnrollmentState.Enrolled)

            else -> {
                coursePaymentsRepository
                    .getCoursePaymentsByCourseId(course.id, coursePaymentStatus = CoursePayment.Status.SUCCESS, sourceType = DataSourceType.REMOTE)
                    .flatMap { payments ->
                        if (payments.isEmpty()) {
                            billingRepository
                                .getInventory(ProductTypes.IN_APP, COURSE_TIER_PREFIX + course.priceTier)
                                .map(::SkuSerializableWrapper)
                                .map(EnrollmentState::NotEnrolledInApp)
                                .cast(EnrollmentState::class.java)
                                .toSingle(EnrollmentState.NotEnrolledWeb)
                                .map { course.id to it }
                        } else {
                            Single.just(course.id to EnrollmentState.NotEnrolledFree)
                        }
                    }
                    .onErrorReturnItem(course.id to EnrollmentState.NotEnrolledWeb) // if billing not supported on current device or to access paid course offline
            }
        }
}
