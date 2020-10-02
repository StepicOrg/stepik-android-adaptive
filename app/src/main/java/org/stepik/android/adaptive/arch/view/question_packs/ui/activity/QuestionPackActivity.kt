package org.stepik.android.adaptive.arch.view.question_packs.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_questions_packs.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.state_error.*
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.arch.domain.question_packs.model.QuestionListItem
import org.stepik.android.adaptive.arch.presentation.question_packs.QuestionPacksPresenter
import org.stepik.android.adaptive.arch.presentation.question_packs.QuestionPacksView
import org.stepik.android.adaptive.arch.presentation.question_packs.model.EnrollmentError
import org.stepik.android.adaptive.arch.view.question_packs.ui.adapter.QuestionPackAdapterDelegate
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BaseActivity
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.ui.adapters.selection.SelectionHelper
import ru.nobird.android.ui.adapters.selection.SingleChoiceSelectionHelper
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class QuestionPackActivity : BaseActivity(), QuestionPacksView {
    companion object {
        const val RESTORE_DIALOG_TAG = "restore_dialog"
    }

    private lateinit var selectionHelper: SelectionHelper

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var billing: Billing

    @Inject
    internal lateinit var questionsPacksManager: QuestionsPacksManager

    private lateinit var presenter: QuestionPacksPresenter
    private lateinit var questionItemAdapter: DefaultDelegateAdapter<QuestionListItem>

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
        viewStateDelegate.addState<QuestionPacksView.State.QuestionPacksLoaded>(recycler, restorePurchases)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions_packs)
        initViewStateDelegate()
        uiCheckout = Checkout.forActivity(this, billing)
        presenter = ViewModelProvider(this, viewModelFactory).get(QuestionPacksPresenter::class.java)
        recycler.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.questions_packs)

        questionItemAdapter = DefaultDelegateAdapter()
        selectionHelper = SingleChoiceSelectionHelper(questionItemAdapter)
        questionItemAdapter += QuestionPackAdapterDelegate(selectionHelper, ::onPackClicked)

        with(recycler) {
            layoutManager = LinearLayoutManager(this@QuestionPackActivity, LinearLayoutManager.VERTICAL, false)
            adapter = questionItemAdapter
        }

        tryAgainButton.setOnClickListener { presenter.loadQuestionListItems(forceUpdate = true) }
        restorePurchases.setOnClickListener { presenter.restoreCoursePurchases() }

        presenter.loadQuestionListItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: QuestionPacksView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is QuestionPacksView.State.QuestionPacksLoaded -> {
                questionItemAdapter.items = state.questionItemList
                state.questionItemList.forEach { questionsPacksManager.onQuestionsPackViewed(it.questionPack) }
                selectionHelper.select(questionsPacksManager.currentPackIndex)
            }
        }
    }

    override fun createUiCheckout(): UiCheckout =
        uiCheckout

    override fun showProgress() {
        showProgressDialogFragment(RESTORE_DIALOG_TAG, getString(R.string.loading_message), getString(R.string.processing_your_request))
    }

    override fun hideProgress() {
        hideProgressDialogFragment(RESTORE_DIALOG_TAG)
    }

    override fun showEnrollmentError(errorType: EnrollmentError) {
        @StringRes
        val errorMessage =
            when (errorType) {
                EnrollmentError.NO_CONNECTION ->
                    R.string.course_error_enroll

                EnrollmentError.SERVER_ERROR ->
                    R.string.course_purchase_server_error

                EnrollmentError.BILLING_ERROR ->
                    R.string.course_purchase_billing_error

                EnrollmentError.BILLING_CANCELLED ->
                    R.string.course_purchase_billing_cancelled

                EnrollmentError.BILLING_NOT_AVAILABLE ->
                    R.string.course_purchase_billing_not_available

                EnrollmentError.COURSE_ALREADY_OWNED ->
                    R.string.course_purchase_already_owned

                EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE ->
                    R.string.course_purchase_billing_no_purchases_to_restore
            }
        Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    override fun changeAfterPurchase(questionListItem: QuestionListItem) {
        selectionHelper.select(questionItemAdapter.items.indexOfFirst { it.id == questionListItem.id })
        presenter.changeCourse(questionListItem.questionPack)
    }

    private fun onPackClicked(sku: Sku?, item: QuestionListItem, isOwned: Boolean) {
        if (sharedPreferenceHelper.fakeUser != null && item.course.isPaid) {
            screenManager.showEmptyAuthScreen(this)
            return
        }
        if (isOwned || !item.course.isPaid) {
            selectionHelper.select(questionItemAdapter.items.indexOfFirst { it.questionPack.id == item.questionPack.id })
            presenter.changeCourse(item.questionPack)
        } else {
            if (sku != null) {
                presenter.purchaseCourse(item.questionPack.courseId, sku)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!uiCheckout.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
