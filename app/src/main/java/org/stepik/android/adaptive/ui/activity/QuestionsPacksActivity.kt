package org.stepik.android.adaptive.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_questions_packs.*
import kotlinx.android.synthetic.main.app_bar.*
import org.solovyev.android.checkout.Checkout
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.QuestionsPacksPresenter
import org.stepik.android.adaptive.core.presenter.contracts.QuestionsPacksView
import org.stepik.android.adaptive.ui.adapter.QuestionsPacksAdapter

class QuestionsPacksActivity : BasePresenterActivity<QuestionsPacksPresenter, QuestionsPacksView>(), QuestionsPacksView {
    companion object {
        const val RESTORE_DIALOG_TAG = "restore_dialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_packs)
        recycler.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.questions_packs)

        restorePurchases.setOnClickListener {
            presenter?.restorePurchases()
        }
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

    override fun onContentLoading() {
        recycler.visibility = View.GONE
        progress.visibility = View.VISIBLE
        purchasesAreNotSupported.visibility = View.GONE
    }

    override fun onContentLoaded() {
        recycler.visibility = View.VISIBLE
        progress.visibility = View.GONE
        purchasesAreNotSupported.visibility = View.GONE
    }

    override fun getBilling() = (application as App).billing

    override fun createCheckout() = Checkout.forActivity(this, getBilling())

    override fun showProgress() =
            showProgressDialogFragment(RESTORE_DIALOG_TAG, getString(R.string.loading_message), getString(R.string.processing_your_request))

    override fun hideProgress() = hideProgressDialogFragment(RESTORE_DIALOG_TAG)

    override fun onAdapter(adapter: QuestionsPacksAdapter) {
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

    override fun getPresenterFactory() = QuestionsPacksPresenter.Companion
}