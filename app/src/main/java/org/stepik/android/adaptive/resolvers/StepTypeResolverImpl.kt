package org.stepik.android.adaptive.resolvers

import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.di.study.StudyScope
import org.stepik.android.adaptive.ui.adapter.attempts.*
import org.stepik.android.adaptive.util.AppConstants
import javax.inject.Inject

@StudyScope
class StepTypeResolverImpl
@Inject
constructor() : StepTypeResolver {
    override fun getAttemptAdapter(step: Step): AttemptAnswerAdapter<*> {
        val type = step.block?.name ?: return NotSupportedQuizAnswerAdapter()

        return when (type) {
            AppConstants.TYPE_CHOICE -> ChoiceQuizAnswerAdapter()
            AppConstants.TYPE_STRING -> StringQuizAnswerAdapter()
            AppConstants.TYPE_NUMBER -> NumberQuizAnswerAdapter()

            else -> NotSupportedQuizAnswerAdapter()
        }
    }
}
