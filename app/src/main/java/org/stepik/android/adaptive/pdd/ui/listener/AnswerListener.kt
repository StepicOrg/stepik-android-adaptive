package org.stepik.android.adaptive.pdd.ui.listener

interface AnswerListener {
    fun onCorrectAnswer(submissionId: Long)
    fun onWrongAnswer()
}