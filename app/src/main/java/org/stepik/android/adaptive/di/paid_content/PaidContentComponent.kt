package org.stepik.android.adaptive.di.paid_content

import dagger.Subcomponent
import org.stepik.android.adaptive.ui.activity.PaidInventoryItemsActivity
import org.stepik.android.adaptive.ui.activity.QuestionsPacksActivity

@Subcomponent
interface PaidContentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): PaidContentComponent
    }

    fun inject(questionsPacksActivity: QuestionsPacksActivity)
    fun inject(paidInventoryItemsActivity: PaidInventoryItemsActivity)
}
