package org.stepik.android.adaptive.content.questions

import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R

enum class QuestionsPack(
        val id: String,
        val courseId: Long,
        var size: Int = 0,
        @StringRes   val difficulty: Int,
        @DrawableRes val background: Int,
        @DrawableRes val icon: Int,
        @ColorInt    val textColor: Int = 0xFFFFFF,
        val hasProgress: Boolean = false
) {
    Basic(
        id          = "questions_pack_basic",
        courseId    = 3149,
        difficulty  = R.string.questions_difficulty_mixed,
        background  = R.drawable.pack_bg_basic,
        icon        = R.drawable.ic_questions_pack_basic,
        textColor   = 0x495057,
        hasProgress = false
    )
}
