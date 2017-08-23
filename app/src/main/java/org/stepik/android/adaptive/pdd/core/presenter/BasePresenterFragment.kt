package org.stepik.android.adaptive.pdd.core.presenter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import org.stepik.android.adaptive.pdd.core.loader.PresenterLoader

abstract class BasePresenterFragment<P : Presenter<V>, in V> : Fragment() {
    private val LOADER_ID = 127
    private var presenter: P? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = loaderManager.getLoader<P>(LOADER_ID)
        if (loader == null) {
            initLoader()
        } else {
            presenter = (loader as PresenterLoader<P>).presenter
            onPresenter(presenter!!)
        }
    }

    private fun initLoader() {
        loaderManager.initLoader(LOADER_ID, null, object : LoaderManager.LoaderCallbacks<P> {
            override fun onLoadFinished(loader: Loader<P>?, data: P) {
                presenter = data
                onPresenter(data)
            }

            override fun onLoaderReset(loader: Loader<P>?) {
                presenter = null
            }

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<P> =
                    PresenterLoader(this@BasePresenterFragment.context, getPresenterFactory())
        })
    }

    protected abstract fun onPresenter(presenter: P)
    protected abstract fun getPresenterFactory() : PresenterFactory<P>
}