package org.stepik.android.adaptive.content.questions.packs

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class MediumQuestionsPack
@Inject
constructor(): QuestionsPack() {
    override val ordinal     = 1
    override val id          = "questions_pack_medium"
    override val courseId    = 6243L

    @StringRes
    override val difficulty  = R.string.questions_difficulty_medium
    @DrawableRes
    override val background  = R.drawable.pack_bg_medium
    @DrawableRes
    override val icon        = R.drawable.ic_questions_pack_medium
}