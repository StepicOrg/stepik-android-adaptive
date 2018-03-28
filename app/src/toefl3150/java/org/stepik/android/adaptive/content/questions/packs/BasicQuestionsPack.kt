package org.stepik.android.adaptive.content.questions.packs

import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class BasicQuestionsPack
@Inject
constructor(): QuestionsPack() {
    override val ordinal     = 0
    override val id          = "questions_pack_basic"
    override val courseId    = 3150L

    @StringRes
    override val difficulty  = R.string.questions_difficulty_mixed
    @DrawableRes
    override val background  = R.drawable.pack_bg_basic
    @DrawableRes
    override val icon        = R.drawable.ic_questions_pack_basic
    @ColorInt
    override val textColor   = 0x495057

    override val isAvailable = true
}