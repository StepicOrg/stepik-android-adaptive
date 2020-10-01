package org.stepik.android.adaptive.content.questions

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.configuration.RemoteConfig
import org.stepik.android.adaptive.di.AppSingleton
import org.stepik.android.adaptive.gamification.ExpManager
import javax.inject.Inject
import kotlin.math.min

@AppSingleton
class QuestionsPacksResolverImpl
@Inject
constructor(
    private val expManager: ExpManager,
    private val context: Context,
    firebaseRemoteConfig: FirebaseRemoteConfig
): QuestionsPacksResolver {
    companion object {
        private const val ADJECTIVES_PACK_TARGET_LEVEL = 10
        private const val VERBS_PACK_TARGET_LEVEL = 12
    }

    private val verbsPacksTargetLevelRecounted = if (firebaseRemoteConfig.getBoolean(RemoteConfig.EXP_LEVEL_FORMULA_EXPERIMENT)) 15 else VERBS_PACK_TARGET_LEVEL

    override fun isAvailableForFree(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Basic, QuestionsPack.Adjectives, QuestionsPack.Verbs -> true
//        QuestionsPack.Adjectives -> expManager.getCurrentLevel(expManager.exp) >= ADJECTIVES_PACK_TARGET_LEVEL
//        QuestionsPack.Verbs      -> expManager.getCurrentLevel(expManager.exp) >= verbsPacksTargetLevelRecounted
        else                     -> false
    }

    override fun calcProgress(pack: QuestionsPack) = when(pack) {
        QuestionsPack.Adjectives -> min(expManager.exp * 100 / expManager.getNextLevelExp(ADJECTIVES_PACK_TARGET_LEVEL - 1L), 100).toInt()
        QuestionsPack.Verbs      -> min(expManager.exp * 100 / expManager.getNextLevelExp(verbsPacksTargetLevelRecounted - 1L), 100).toInt()
        else -> 0
    }

    override fun getProgressDescription(pack: QuestionsPack): String = when(pack) {
        QuestionsPack.Adjectives -> context.getString(R.string.questions_progress_description_level, ADJECTIVES_PACK_TARGET_LEVEL)
        QuestionsPack.Verbs      -> context.getString(R.string.questions_progress_description_level, verbsPacksTargetLevelRecounted)
        else -> String()
    }
}