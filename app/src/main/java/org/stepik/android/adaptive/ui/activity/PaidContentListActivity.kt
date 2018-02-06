package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.recycler_view.*
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.PaidContentPresenter
import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView

class PaidContentListActivity : BasePresenterActivity<PaidContentPresenter, PaidContentView>(), PaidContentView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_view)

        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun getPresenterFactory() = PaidContentPresenter.Companion
}