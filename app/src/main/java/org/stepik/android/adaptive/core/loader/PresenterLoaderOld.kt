package org.stepik.android.adaptive.core.loader

import android.content.Context
import android.support.v4.content.Loader
import org.stepik.android.adaptive.core.presenter.Presenter
import org.stepik.android.adaptive.core.presenter.PresenterFactory

class PresenterLoaderOld<P: Presenter<*>>(
        appContext: Context,
        private val presenterFactory: PresenterFactory<P>
) : Loader<P>(appContext) {

    var presenter: P? = null
        private set

    override fun onStartLoading() {
        if (presenter != null) {
            deliverResult(presenter)
            return
        }

        forceLoad()
    }

    override fun onForceLoad() {
        presenter = presenterFactory.create()
        deliverResult(presenter)
    }

    override fun onReset() {
        presenter?.destroy()
        presenter = null
    }
}