package org.stepik.android.adaptive.resolvers

import org.stepik.android.adaptive.data.model.Step
import org.stepik.android.adaptive.ui.adapter.attempts.AttemptAnswerAdapter

interface StepTypeResolver {
    fun getAttemptAdapter(step: Step): AttemptAnswerAdapter<*>
}