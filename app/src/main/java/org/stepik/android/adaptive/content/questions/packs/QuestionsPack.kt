package org.stepik.android.adaptive.content.questions.packs

abstract class QuestionsPack internal constructor() {
    abstract val ordinal: Int
    abstract val id: String
    abstract val courseId: Long
    open var size: Int = 0

    abstract val difficulty: Int
    abstract val background: Int

    abstract val icon: Int

    open val textColor: Int = 0xFFFFFF

    open val hasProgress: Boolean = false
    open fun calcProgress(): Int = 0

    open val isAvailable: Boolean = false
}