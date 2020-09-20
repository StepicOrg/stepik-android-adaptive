package org.stepik.android.adaptive.ui.listener

interface AnswerListener {
    fun onCorrectAnswer(submissionId: Long)
    fun onWrongAnswer()
}
