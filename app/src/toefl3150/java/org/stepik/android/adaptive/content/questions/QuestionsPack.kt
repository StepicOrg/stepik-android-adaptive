package org.stepik.android.adaptive.content.questions

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.stepik.android.adaptive.R

enum class QuestionsPack(
        val id: String,
        val courseId: Long,
        var size: Int = 0,
        @StringRes   val difficulty: Int,
        @DrawableRes val background: Int,
        @DrawableRes val icon: Int,
        @ColorInt    val textColor: Int = 0xFFFFFF,
        val hasProgress: Boolean = false) {
    Basic(
            id          = "questions_pack_basic",
            courseId    = 3150,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_basic,
            icon        = R.drawable.ic_questions_pack_basic,
            textColor   = 0x495057
    ),

    Medium(
            id          = "questions_pack_medium",
            courseId    = 6243,
            difficulty  = R.string.questions_difficulty_medium,
            background  = R.drawable.pack_bg_medium,
            icon        = R.drawable.ic_questions_pack_medium
    ),

    Pro(
            id          = "questions_pack_pro",
            courseId    = 6312,
            difficulty  = R.string.questions_difficulty_high,
            background  = R.drawable.pack_bg_pro,
            icon        = R.drawable.ic_questions_pack_pro
    ),

    Full(
            id          = "questions_pack_full",
            courseId    = 6315,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_full,
            icon        = R.drawable.ic_questions_pack_full
    ),

    Adjectives(
            id          = "questions_pack_adjectives",
            courseId    = 3149,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_adjectives,
            icon        = R.drawable.ic_questions_pack_adjectives,
            hasProgress = true
    ),

    Verbs(
            id          = "questions_pack_verbs",
            courseId    = 3124,
            difficulty  = R.string.questions_difficulty_mixed,
            background  = R.drawable.pack_bg_verbs,
            icon        = R.drawable.ic_questions_pack_verbs,
            hasProgress = true
    );
}