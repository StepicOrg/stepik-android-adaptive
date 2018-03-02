package org.stepik.android.adaptive.data.model

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
        @ColorInt    val textColor: Int = 0xFFFFFF,
        val isFree: Boolean = false) {
    Basic(
            id          = "questions_pack_basic",
            courseId    = 3150,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_basic,
            textColor   = 0x495057,
            isFree      = true
    ),

    Medium(
            id          = "questions_pack_medium",
            courseId    = 6243,
            difficulty  = R.string.questions_difficulty_medium,
            background  = R.drawable.pack_bg_medium
    ),

    Pro(
            id          = "questions_pack_pro",
            courseId    = 6312,
            difficulty  = R.string.questions_difficulty_high,
            background  = R.drawable.pack_bg_pro
    ),

    Full(
            id          = "questions_pack_full",
            courseId    = 6315,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_full
    );

    companion object {
        fun getById(id: String) = values().find { it.id == id }
    }
}