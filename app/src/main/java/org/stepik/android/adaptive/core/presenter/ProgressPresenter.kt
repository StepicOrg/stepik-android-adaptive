package org.stepik.android.adaptive.core.presenter

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.core.presenter.contracts.ProgressView
import org.stepik.android.adaptive.data.db.DataBaseMgr
import org.stepik.android.adaptive.ui.adapter.WeeksAdapter
import org.stepik.android.adaptive.util.ExpUtil

class ProgressPresenter : PresenterBase<ProgressView>() {
    companion object : PresenterFactory<ProgressPresenter> {
        override fun create() = ProgressPresenter()
    }

    private val total by lazy { ExpUtil.getExp() }
    private val level by lazy { ExpUtil.getCurrentLevel(total) }

    private val adapter = WeeksAdapter()

    private val composite = CompositeDisposable()

    init {
        adapter.setHeaderLevelAndTotal(level, total)

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
                            adapter.setHeaderChart(it.first, it.second)
                        }, {})
        )
    }

    override fun attachView(view: ProgressView) {
        super.attachView(view)
        view.onWeeksAdapter(adapter)
    }

    override fun destroy() {}
}