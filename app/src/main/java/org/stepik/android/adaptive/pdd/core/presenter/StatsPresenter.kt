package org.stepik.android.adaptive.pdd.core.presenter

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.pdd.core.presenter.contracts.StatsView
import org.stepik.android.adaptive.pdd.data.db.DataBaseMgr
import org.stepik.android.adaptive.pdd.ui.adapter.WeeksAdapter
import org.stepik.android.adaptive.pdd.util.ExpUtil

class StatsPresenter : PresenterBase<StatsView>() {
    companion object : PresenterFactory<StatsPresenter> {
        override fun create() = StatsPresenter()
    }

    private val total by lazy { ExpUtil.getExp() }
    private val level by lazy { ExpUtil.getCurrentLevel(total) }

    private val adapter = WeeksAdapter()

    private var last7Days = -1L

    private val composite = CompositeDisposable()

    private var dataSet: LineDataSet? = null

    init {
        composite.add(
            Observable.fromCallable(DataBaseMgr.instance::getWeeks)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(adapter::addAll, {})
        )

        composite.add(
                Observable.fromCallable(DataBaseMgr.instance::getExpForLast7Days)
                        .map {
                            Pair(LineDataSet(it.mapIndexed { index, l -> Entry(index.toFloat(), l.toFloat()) }, ""), it.sum())
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            onDataSet(it.first)
                            onLast7Days(it.second)
                        }, {})
        )
    }

    private fun onDataSet(ds: LineDataSet) {
        this.dataSet = ds
        view?.onChartData(ds)
    }

    private fun onLast7Days(exp: Long) {
        this.last7Days = exp
        view?.onLast7Days(exp)
    }

    override fun attachView(view: StatsView) {
        super.attachView(view)
        view.onTotal(total)
        view.onLevel(level)
        view.onWeeksAdapter(adapter)
        if (last7Days != -1L) {
            view.onLast7Days(last7Days)
        }
        dataSet?.let { view.onChartData(it) }
    }

    override fun destroy() {}
}