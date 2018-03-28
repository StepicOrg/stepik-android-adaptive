package org.stepik.android.adaptive.content.questions.packs

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class FullQuestionsPack
@Inject
constructor(): QuestionsPack() {
    override val ordinal     = 3
    override val id          = "questions_pack_full"
    override val courseId    = 6315L

    @StringRes
    override val difficulty  = R.string.questions_difficulty_mixed
    @DrawableRes
    override val background  = R.drawable.pack_bg_full
    @DrawableRes
    override val icon        = R.drawable.ic_questions_pack_full
}