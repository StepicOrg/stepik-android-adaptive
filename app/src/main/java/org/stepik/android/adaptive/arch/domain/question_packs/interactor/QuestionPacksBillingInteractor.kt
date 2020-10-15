package org.stepik.android.adaptive.arch.domain.question_packs.interactor

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes
import io.reactivex.rxkotlin.toObservable
import okhttp3.ResponseBody
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.arch.domain.base.DataSourceType
import org.stepik.android.adaptive.arch.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.adaptive.arch.domain.billing.extension.startPurchaseFlowRx
import org.stepik.android.adaptive.arch.domain.billing.model.CoursePurchasePayload
import org.stepik.android.adaptive.arch.domain.billing.repository.BillingRepository
import org.stepik.android.adaptive.arch.domain.course_payments.exception.CourseAlreadyOwnedException
import org.stepik.android.adaptive.arch.domain.course_payments.exception.CoursePurchaseVerificationException
import org.stepik.android.adaptive.arch.domain.course_payments.model.CoursePayment
import org.stepik.android.adaptive.arch.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.util.toObject
import retrofit2.HttpException
import retrofit2.Response
import ru.nobird.android.domain.rx.maybeFirst
import java.net.HttpURLConnection
import javax.inject.Inject

class QuestionPacksBillingInteractor
@Inject
constructor(
    private val api: Api,
    private val billingRepository: BillingRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,

    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
) {
    private val gson = Gson()

    companion object {
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
    }

    fun purchaseCourse(checkout: UiCheckout, courseId: Long, sku: Sku): Completable =
        coursePaymentsRepository
            .getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS, sourceType = DataSourceType.REMOTE)
            .flatMapCompletable { payments ->
                if (payments.isEmpty()) {
                    purchaseCourseAfterCheck(checkout, courseId, sku)
                } else {
                    Completable.error(CourseAlreadyOwnedException(courseId))
                }
            }

    private fun purchaseCourseAfterCheck(checkout: UiCheckout, courseId: Long, sku: Sku): Completable =
        getCurrentProfileId()
            .map { profileId ->
                gson.toJson(CoursePurchasePayload(profileId, courseId))
            }
            .observeOn(mainScheduler)
            .flatMap { payload ->
                checkout.startPurchaseFlowRx(sku, payload)
            }
            .observeOn(backgroundScheduler)
            .flatMapCompletable { purchase ->
                completePurchase(courseId, sku, purchase)
            }

    fun restorePurchases(skus: List<Sku>): Completable =
        skus
            .toObservable()
            .flatMapCompletable { restorePurchase(it) }

    private fun restorePurchase(sku: Sku): Completable =
        Maybes.zip(
            getCurrentProfileId()
                .toMaybe(),
            billingRepository
                .getAllPurchases(ProductTypes.IN_APP, listOf(sku.id.code))
                .observeOn(backgroundScheduler)
                .maybeFirst()
        )
            .map { (profileId, purchase) ->
                Triple(profileId, purchase, purchase.payload.toObject<CoursePurchasePayload>(gson))
            }
            .filter { (profileId, _, payload) ->
                profileId == payload.profileId
            }
            .flatMapCompletable { (_, purchase, payload) ->
                completePurchase(payload.courseId, sku, purchase)
            }

    private fun completePurchase(courseId: Long, sku: Sku, purchase: Purchase): Completable =
        coursePaymentsRepository
            .createCoursePayment(courseId, sku, purchase)
            .flatMapCompletable { payment ->
                if (payment.status == CoursePayment.Status.SUCCESS) {
                    Completable.complete()
                } else {
                    Completable.error(CoursePurchaseVerificationException())
                }
            }
            .andThen(api.joinCourse(courseId))
            .andThen(billingRepository.consumePurchase(purchase))

    private fun getCurrentProfileId(): Single<Long> =
        Single.fromCallable {
            sharedPreferenceHelper
                .profile
                ?.id
                ?: throw UNAUTHORIZED_EXCEPTION_STUB
        }
}
