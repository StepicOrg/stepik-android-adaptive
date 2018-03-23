package org.stepik.android.adaptive.core.presenter

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import org.stepik.android.adaptive.core.loader.PresenterLoaderOld

abstract class BasePresenterFragmentOld<P : Presenter<V>, in V> : Fragment() {
    private val LOADER_ID = 127
    protected var presenter: P? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = loaderManager.getLoader<P>(LOADER_ID)
        if (loader == null) {
            initLoader()
        } else {
            onPresenter((loader as PresenterLoaderOld<P>).presenter)
        }
    }

    private fun initLoader() {
        loaderManager.initLoader(LOADER_ID, null, object : LoaderManager.LoaderCallbacks<P> {
            override fun onLoadFinished(loader: Loader<P>?, data: P) {
                onPresenter(data)
            }

            override fun onLoaderReset(loader: Loader<P>?) {
                presenter = null
            }

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<P> =
                    PresenterLoaderOld(this@BasePresenterFragmentOld.context, getPresenterFactory())
        })
    }

    @CallSuper
    protected open fun onPresenter(presenter: P?) {
        this.presenter = presenter
    }

    protected abstract fun getPresenterFactory() : PresenterFactory<P>
}