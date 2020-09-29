package org.stepik.android.adaptive.arch.domain.question_packs.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.adaptive.arch.domain.billing.model.SkuSerializableWrapper

sealed class EnrollmentState : Parcelable {
    @Parcelize
    object Enrolled : EnrollmentState()

    @Parcelize
    object NotEnrolledFree : EnrollmentState()

    @Parcelize
    data class NotEnrolledInApp(val skuWrapper: SkuSerializableWrapper) : EnrollmentState()

    @Parcelize
    object NotEnrolledWeb : EnrollmentState()
}
