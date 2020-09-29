package org.stepik.android.adaptive.arch.domain.question_packs.model

import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.data.model.Course

data class QuestionListItem(
    val course: Course,
    val questionPack: QuestionsPack,
    val enrollmentState: EnrollmentState
)