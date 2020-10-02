package org.stepik.android.adaptive.arch.presentation.question_packs.mapper

import org.solovyev.android.checkout.BillingException
import org.solovyev.android.checkout.ResponseCodes
import org.stepik.android.adaptive.arch.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.adaptive.arch.domain.course_payments.exception.CourseAlreadyOwnedException
import org.stepik.android.adaptive.arch.domain.course_payments.exception.CoursePurchaseVerificationException
import org.stepik.android.adaptive.arch.presentation.question_packs.model.EnrollmentError
import retrofit2.HttpException
import java.net.HttpURLConnection

fun Throwable.toEnrollmentError(): EnrollmentError =
    when (this) {
        is HttpException ->
            when (code()) {
                HttpURLConnection.HTTP_BAD_REQUEST ->
                    EnrollmentError.SERVER_ERROR

                else ->
                    EnrollmentError.NO_CONNECTION
            }

        is BillingException ->
            when (response) {
                ResponseCodes.USER_CANCELED ->
                    EnrollmentError.BILLING_CANCELLED

                ResponseCodes.BILLING_UNAVAILABLE ->
                    EnrollmentError.BILLING_NOT_AVAILABLE

                else ->
                    EnrollmentError.BILLING_ERROR
            }

        is CoursePurchaseVerificationException ->
            EnrollmentError.SERVER_ERROR

        is CourseAlreadyOwnedException ->
            EnrollmentError.COURSE_ALREADY_OWNED

        is NoPurchasesToRestoreException ->
            EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE

        else ->
            EnrollmentError.NO_CONNECTION
    }
