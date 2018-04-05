package org.stepik.android.adaptive.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_paid_content_list.*
import kotlinx.android.synthetic.main.app_bar.*
import org.solovyev.android.checkout.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.PaidInventoryItemsPresenter
import org.stepik.android.adaptive.core.presenter.contracts.PaidInventoryItemsView
import org.stepik.android.adaptive.ui.adapter.PaidInventoryAdapter
import org.stepik.android.adaptive.ui.dialog.InventoryDialog
import org.stepik.android.adaptive.gamification.InventoryManager
import javax.inject.Inject
import javax.inject.Provider

class PaidInventoryItemsActivity : BasePresenterActivity<PaidInventoryItemsPresenter, PaidInventoryItemsView>(), PaidInventoryItemsView {
    companion object {
        const val INVENTORY_DIALOG_TAG = "inventory_dialog"
        const val RESTORE_DIALOG_TAG = "restore_dialog"
    }

    @Inject
    lateinit var billing: Billing

    @Inject
    lateinit var inventoryManager: InventoryManager

    @Inject
    lateinit var paidInventoryItemsPresenterProvider: Provider<PaidInventoryItemsPresenter>

    override fun injectComponent() {
        App.componentManager().paidContentComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paid_content_list)

        recycler.layoutManager = LinearLayoutManager(this)

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.stroke))
        recycler.addItemDecoration(divider)

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

    override fun onPurchasesNotSupported() {
        recycler.visibility = View.GONE
        progress.visibility = View.GONE
        purchasesAreNotSupported.visibility = View.VISIBLE
        restorePurchases.visibility = View.GONE
    }

    override fun showContentProgress() {
        recycler.visibility = View.GONE
        progress.visibility = View.VISIBLE
        purchasesAreNotSupported.visibility = View.GONE
    }

    override fun hideContentProgress() {
        recycler.visibility = View.VISIBLE
        progress.visibility = View.GONE
        purchasesAreNotSupported.visibility = View.GONE
    }

    override fun createCheckout() = Checkout.forActivity(this, billing)

    override fun showInventoryDialog() = InventoryDialog().show(supportFragmentManager, INVENTORY_DIALOG_TAG)

    override fun showProgress() =
            showProgressDialogFragment(RESTORE_DIALOG_TAG, getString(R.string.loading_message), getString(R.string.processing_your_request))

    override fun hideProgress() = hideProgressDialogFragment(RESTORE_DIALOG_TAG)

    override fun onAdapter(adapter: PaidInventoryAdapter) {
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

    override fun finish() {
        if (inventoryManager.hasTickets()) {
            setResult(Activity.RESULT_OK)
        }
        super.finish()
    }

    override fun getPresenterProvider() = paidInventoryItemsPresenterProvider
}