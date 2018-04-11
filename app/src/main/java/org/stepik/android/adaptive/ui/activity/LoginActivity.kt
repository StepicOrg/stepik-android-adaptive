package org.stepik.android.adaptive.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_login.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.LoginPresenter
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.ui.dialog.RemindPasswordDialog
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.setOnKeyboardOpenListener
import javax.inject.Inject
import javax.inject.Provider

class LoginActivity : BasePresenterActivity<LoginPresenter, LoginView>(), LoginView {
    private companion object {
        private const val PROGRESS = "login_progress"
        private const val REMIND_PASSWORD_DIALOG = "remind_password_dialog"

        private const val EMAIL_KEY = "email"
    }

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var loginPresenterProvider: Provider<LoginPresenter>

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initTitle()

        loginField.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordField.requestFocus()
                handled = true
            }
            handled
        }

        passwordField.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                tryLogin()
                handled = true
            }
            handled
        }

        loginRootView.requestFocus()

        if (savedInstanceState == null && intent.hasExtra(EMAIL_KEY)) {
            loginField.setText(intent.getStringExtra(EMAIL_KEY))
            passwordField.requestFocus()
        }

        loginButton.setOnClickListener {
            tryLogin()
        }

        remindPasswordButton.setOnClickListener {
            RemindPasswordDialog().show(supportFragmentManager, REMIND_PASSWORD_DIALOG)
        }

        setOnKeyboardOpenListener(
                root_view,
                onKeyboardShown = { signInText.changeVisibillity(false) },
                onKeyboardHidden = { signInText.changeVisibillity(true) }
        )
    }

    private fun initTitle() {
        val signInString = getString(R.string.sign_in)
        val signInWithPasswordSuffix = getString(R.string.sign_in_with_password_suffix)

        val spannableSignIn = SpannableString(signInString + signInWithPasswordSuffix)
        val typefaceSpan = StyleSpan(Typeface.BOLD)

        spannableSignIn.setSpan(typefaceSpan, 0, signInString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        signInText.text = spannableSignIn
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if ((item?.itemId ?: -1) == android.R.id.home) {
            Util.hideSoftKeyboard(this)
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    private fun tryLogin() {
        val login = loginField.text.toString()
        val password = passwordField.text.toString()

        presenter?.authWithLoginPassword(login, password)
    }

    override fun onSuccess() {
        screenManager.startStudy()
    }

    override fun onNetworkError() {
        hideProgressDialogFragment(PROGRESS)
//        Snackbar.make(binding.root, R.string.auth_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onError(errorBody: String) {
        hideProgressDialogFragment(PROGRESS)
    }

    override fun onLoading() {
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_in), getString(R.string.processing_your_request))
    }

    override fun getPresenterProvider() = loginPresenterProvider
}