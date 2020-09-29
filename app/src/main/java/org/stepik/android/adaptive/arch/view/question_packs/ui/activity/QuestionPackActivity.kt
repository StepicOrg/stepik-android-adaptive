package org.stepik.android.adaptive.arch.view.question_packs.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_questions_packs.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.state_error.*
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.arch.presentation.question_packs.QuestionPacksPresenter
import org.stepik.android.adaptive.arch.presentation.question_packs.QuestionPacksView
import org.stepik.android.adaptive.arch.view.ui.delegate.ViewStateDelegate
import org.stepik.android.adaptive.core.presenter.BaseActivity
import org.stepik.android.adaptive.ui.activity.QuestionsPacksActivity
import javax.inject.Inject

class QuestionPackActivity : BaseActivity(), QuestionPacksView {
    companion object {
        const val RESTORE_DIALOG_TAG = "restore_dialog"
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var billing: Billing

    private lateinit var presenter: QuestionPacksPresenter

    private lateinit var uiCheckout: UiCheckout

    private val viewStateDelegate = ViewStateDelegate<QuestionPacksView.State>()

    private fun injectComponent() {
        App.componentManager().paidContentComponent.inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<QuestionPacksView.State.Idle>()
        viewStateDelegate.addState<QuestionPacksView.State.Loading>(progress)
        viewStateDelegate.addState<QuestionPacksView.State.Error>(errorState)
        viewStateDelegate.addState<QuestionPacksView.State.PurchasesNotSupported>(purchasesAreNotSupported)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_packs)
        presenter = ViewModelProvider(this, viewModelFactory).get(QuestionPacksPresenter::class.java)
        recycler.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.questions_packs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setState(state: QuestionPacksView.State) {
        viewStateDelegate.switchState(state)
    }

    override fun createUiCheckout(): UiCheckout =
        uiCheckout

    override fun showProgress() {
        showProgressDialogFragment(QuestionsPacksActivity.RESTORE_DIALOG_TAG, getString(R.string.loading_message), getString(R.string.processing_your_request))
    }

    override fun hideProgress() {
        hideProgressDialogFragment(QuestionsPacksActivity.RESTORE_DIALOG_TAG)
    }
}