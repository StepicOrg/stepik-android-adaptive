package org.stepik.android.adaptive.arch.domain.question_packs.model

import org.stepik.android.adaptive.content.questions.QuestionsPack
import org.stepik.android.adaptive.data.model.Course
import ru.nobird.android.core.model.Identifiable

data class QuestionListItem(
    val course: Course,
    val questionPack: QuestionsPack,
    val enrollmentState: EnrollmentState
) : Identifiable<Long> {
    override val id: Long =
        course.id
}
