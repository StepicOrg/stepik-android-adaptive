package org.stepik.android.adaptive.core.presenter

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import org.stepik.android.adaptive.core.presenter.contracts.ProgressView
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.ExpManager
import org.stepik.android.adaptive.ui.adapter.WeeksAdapter
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProgressPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val expManager: ExpManager,
    private val dataBaseMgr: DataBaseMgr
) : PresenterBase<ProgressView>() {
    private val adapter = WeeksAdapter()

    init {
        compositeDisposable += expManager.fetchExp()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe { exp ->
                val level = expManager.getCurrentLevel(exp)
                adapter.setHeaderLevelAndTotal(level = level, total = exp)
            }

        compositeDisposable += dataBaseMgr.getWeeks()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe(adapter::addAll, {})

        compositeDisposable += dataBaseMgr.getExpForLast7Days()
            .map {
                Pair(LineDataSet(it.mapIndexed { index, l -> Entry(index.toFloat(), l.toFloat()) }, ""), it.sum())
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe(
                {
                    adapter.setHeaderChart(it.first, it.second)
                },
                {}
            )
    }

    override fun attachView(view: ProgressView) {
        super.attachView(view)
        view.onWeeksAdapter(adapter)
    }
}
