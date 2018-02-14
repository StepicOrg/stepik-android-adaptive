package org.stepik.android.adaptive.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_paid_content_list.*
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.PaidContentPresenter
import org.stepik.android.adaptive.core.presenter.contracts.PaidContentView
import org.stepik.android.adaptive.ui.adapter.PaidContentAdapter
import org.stepik.android.adaptive.ui.dialog.InventoryDialog

class PaidContentListActivity : BasePresenterActivity<PaidContentPresenter, PaidContentView>(), PaidContentView {
    companion object {
        const val INVENTORY_DIALOG_TAG = "inventory_dialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paid_content_list)

        recycler.layoutManager = LinearLayoutManager(this)

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        tryAgain.setOnClickListener {
            presenter?.loadInventory()
        }

        restorePurchases.setOnClickListener {
            presenter?.restorePurchases()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.paid_items)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPurchaseError() {
        Snackbar.make(root, R.string.purchase_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onInventoryError() {
        recycler.visibility = View.GONE
        error.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

    override fun onInventoryLoading() {
        recycler.visibility = View.GONE
        error.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    override fun onInventoryLoaded() {
        recycler.visibility = View.VISIBLE
        error.visibility = View.GONE
        progress.visibility = View.GONE
    }

    override fun getBilling() = (application as App).billing

    override fun createCheckout() = Checkout.forActivity(this, getBilling())

    override fun showInventoryDialog() = InventoryDialog().show(supportFragmentManager, INVENTORY_DIALOG_TAG)

    override fun onAdapter(adapter: PaidContentAdapter) {
        recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun getPresenterFactory() = PaidContentPresenter.Companion
}