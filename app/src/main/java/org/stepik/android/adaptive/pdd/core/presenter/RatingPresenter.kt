package org.stepik.android.adaptive.pdd.core.presenter

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.stepik.android.adaptive.pdd.api.API
import org.stepik.android.adaptive.pdd.core.presenter.contracts.RatingView
import org.stepik.android.adaptive.pdd.data.model.RatingItem
import org.stepik.android.adaptive.pdd.ui.adapter.RatingAdapter
import org.stepik.android.adaptive.pdd.util.RatingNamesGenerator

class RatingPresenter : PresenterBase<RatingView>() {
    companion object : PresenterFactory<RatingPresenter> {
        override fun create() = RatingPresenter()
    }

    private val adapter = RatingAdapter()

    private var isError = false

    init {
        API.getInstance().getRating(50, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ adapter.add(it.mapIndexed { index, (rank, _, exp, user) ->
                    RatingItem(
                            if (rank == 0) index + 1 else rank,
                            RatingNamesGenerator.getName(user),
                            exp,
                            user
                    )
                }) }, { onError() })
    }

    override fun attachView(view: RatingView) {
        super.attachView(view)

        view.onRatingAdapter(adapter)
    }

    private fun onError() {
        isError = true
        view?.onError()
    }

    override fun destroy() {}
}