package org.stepik.android.adaptive.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_questions_packs.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.state_error.*
import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.arch.presentation.question_packs.QuestionPacksPresenter
import org.stepik.android.adaptive.core.presenter.BaseActivity
import org.stepik.android.adaptive.core.presenter.contracts.QuestionsPacksView
import org.stepik.android.adaptive.ui.adapter.QuestionsPacksAdapter
import org.stepik.android.adaptive.util.changeVisibillity
import javax.inject.Inject

class QuestionsPacksActivity : BaseActivity(), QuestionsPacksView {
    companion object {
        const val RESTORE_DIALOG_TAG = "restore_dialog"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var billing: Billing

    private lateinit var presenter: QuestionPacksPresenter

    private lateinit var uiCheckout: UiCheckout

    private fun injectComponent() {
        App.componentManager().paidContentComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_packs)

//        presenter = ViewModelProvider(this, viewModelFactory).get(QuestionPacksPresenter::class.java)
        recycler.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.questions_packs)

//        uiCheckout = Checkout.forActivity(this, billing)

        restorePurchases.setOnClickListener {
//            presenter.restorePurchases()
        }

        tryAgainButton.setOnClickListener {
//            presenter.loadContent()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createUiCheckout(): UiCheckout =
        uiCheckout

    override fun onPurchaseError() {
        Snackbar.make(root, R.string.purchase_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onPurchasesNotSupported() {
        recycler.changeVisibillity(false)
        progress.changeVisibillity(false)
        purchasesAreNotSupported.changeVisibillity(true)
        restorePurchases.changeVisibillity(false)
        errorState.changeVisibillity(false)
    }

    override fun showContentProgress() {
        recycler.changeVisibillity(false)
        progress.changeVisibillity(true)
        purchasesAreNotSupported.changeVisibillity(false)
        errorState.changeVisibillity(false)
    }

    override fun hideContentProgress() {
        recycler.changeVisibillity(true)
        progress.changeVisibillity(false)
        purchasesAreNotSupported.changeVisibillity(false)
        errorState.changeVisibillity(false)
    }

    override fun onContentError() {
        recycler.changeVisibillity(false)
        progress.changeVisibillity(false)
        purchasesAreNotSupported.changeVisibillity(false)
        errorState.changeVisibillity(true)
    }

    override fun createCheckout(): ActivityCheckout =
        throw UnsupportedOperationException("Replaced with new code")

    override fun showProgress() {
        showProgressDialogFragment(RESTORE_DIALOG_TAG, getString(R.string.loading_message), getString(R.string.processing_your_request))
    }

    override fun hideProgress() {
        hideProgressDialogFragment(RESTORE_DIALOG_TAG)
    }

    override fun onAdapter(adapter: QuestionsPacksAdapter) {
        recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
//        presenter.attachView(this)
    }

    override fun onStop() {
//        presenter.detachView(this)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!uiCheckout.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
