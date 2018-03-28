package org.stepik.android.adaptive.content.questions.packs

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.ExpManager
import javax.inject.Inject
import kotlin.math.min

@AppSingleton
class VerbsQuestionsPack
@Inject
constructor(
        private val expManager: ExpManager
): QuestionsPack() {
    companion object {
        private const val TARGET_LEVEL = 12
    }

    override val ordinal     = 5

    override val id          = "questions_pack_verbs"
    override val courseId    = 3124L

    @StringRes
    override val difficulty  = R.string.questions_difficulty_mixed
    @DrawableRes
    override val background  = R.drawable.pack_bg_verbs
    @DrawableRes
    override val icon        = R.drawable.ic_questions_pack_verbs

    override val hasProgress = true
    override val isAvailable = expManager.getCurrentLevel(expManager.exp) >= TARGET_LEVEL

    override fun calcProgress() = min(expManager.exp * 100 / expManager.getNextLevelExp(TARGET_LEVEL - 1L), 100).toInt()
}