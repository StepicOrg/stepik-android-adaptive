package org.stepik.android.adaptive.core.presenter

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.core.presenter.contracts.ProgressView
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.ui.adapter.WeeksAdapter
import org.stepik.android.adaptive.util.ExpUtil
import javax.inject.Inject

class ProgressPresenter
@Inject
constructor(
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) : PresenterBase<ProgressView>() {

    private val total by lazy { ExpUtil.getExp() }
    private val level by lazy { ExpUtil.getCurrentLevel(total) }

    private val adapter = WeeksAdapter()

    private val composite = CompositeDisposable()

    init {
        adapter.setHeaderLevelAndTotal(level, total)

        composite.add(
            Observable.fromCallable(DataBaseMgr.instance::getWeeks)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribe(adapter::addAll, {})
        )

        composite.add(
                Observable.fromCallable(DataBaseMgr.instance::getExpForLast7Days)
                        .map {
                            Pair(LineDataSet(it.mapIndexed { index, l -> Entry(index.toFloat(), l.toFloat()) }, ""), it.sum())
                        }
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe({
                            adapter.setHeaderChart(it.first, it.second)
                        }, {})
        )
    }

    override fun attachView(view: ProgressView) {
        super.attachView(view)
        view.onWeeksAdapter(adapter)
    }

    override fun destroy() {
        composite.dispose()
    }
}