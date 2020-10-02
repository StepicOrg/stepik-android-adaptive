package org.stepik.android.adaptive.di.paid_content

import dagger.Subcomponent
import org.stepik.android.adaptive.arch.view.question_packs.ui.activity.QuestionPackActivity
import org.stepik.android.adaptive.ui.activity.PaidInventoryItemsActivity

@Subcomponent
interface PaidContentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): PaidContentComponent
    }

    fun inject(questionPackActivity: QuestionPackActivity)
    fun inject(paidInventoryItemsActivity: PaidInventoryItemsActivity)
}
