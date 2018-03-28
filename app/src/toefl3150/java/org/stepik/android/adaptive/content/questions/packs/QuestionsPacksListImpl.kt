package org.stepik.android.adaptive.content.questions.packs

import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class QuestionsPacksListImpl
@Inject
constructor(
        basicQuestionsPack: BasicQuestionsPack,
        mediumQuestionsPack: MediumQuestionsPack,
        proQuestionsPack: ProQuestionsPack,
        fullQuestionsPack: FullQuestionsPack,
        adjectivesQuestionsPack: AdjectivesQuestionsPack,
        verbsQuestionsPack: VerbsQuestionsPack
): QuestionsPacksList {

    override val questionsPacks = listOf(
            basicQuestionsPack,
            mediumQuestionsPack,
            proQuestionsPack,
            fullQuestionsPack,
            adjectivesQuestionsPack,
            verbsQuestionsPack
    )

}