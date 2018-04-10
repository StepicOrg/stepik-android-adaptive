package org.stepik.android.adaptive.data.db.dao

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.adaptive.data.model.LocalExpItem
import org.stepik.android.adaptive.data.model.WeekProgress

interface ExpDao: IDao<LocalExpItem> {
    fun getExpItem(submissionId: Long = -1): Maybe<LocalExpItem>

    fun getExp(): Single<Long>
    fun getExpForLast7Days(): Single<Array<Long>>
    fun getWeeks(): Single<List<WeekProgress>>
}