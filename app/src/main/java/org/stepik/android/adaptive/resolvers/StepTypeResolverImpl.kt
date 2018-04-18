package org.stepik.android.adaptive.resolvers

import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.di.study.StudyScope
import org.stepik.android.adaptive.ui.adapter.attempts.AttemptAnswerAdapter
import org.stepik.android.adaptive.ui.adapter.attempts.ChoiceQuizAnswersAdapter
import org.stepik.android.adaptive.ui.adapter.attempts.NotSupportedQuizAnswerAdapter
import org.stepik.android.adaptive.ui.adapter.attempts.StringQuizAnswerAdapter
import org.stepik.android.adaptive.util.AppConstants
import javax.inject.Inject

@StudyScope
class StepTypeResolverImpl
@Inject
constructor(): StepTypeResolver {
    override fun getAttemptAdapter(step: Step): AttemptAnswerAdapter<*> {
        val type = step.block?.name ?: return NotSupportedQuizAnswerAdapter()

        return when (type) {
            AppConstants.TYPE_CHOICE -> ChoiceQuizAnswersAdapter()
            AppConstants.TYPE_STRING -> StringQuizAnswerAdapter()

            else -> NotSupportedQuizAnswerAdapter()
        }
    }
}