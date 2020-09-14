package org.stepik.android.adaptive.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.annotation.StringRes
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_login.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.api.auth.AuthError
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BaseActivity
import org.stepik.android.adaptive.core.presenter.AuthPresenter
import org.stepik.android.adaptive.core.presenter.contracts.AuthView
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.ui.dialog.RemindPasswordDialog
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.fromHtmlCompat
import org.stepik.android.adaptive.util.hideSoftKeyboard
import org.stepik.android.adaptive.util.setOnKeyboardOpenListener
import javax.inject.Inject

class LoginActivity : BaseActivity(), AuthView {
    companion object {
        private const val PROGRESS = "login_progress"
        private const val REMIND_PASSWORD_DIALOG = "remind_password_dialog"

        private const val EMAIL_KEY = "email"

        const val REQUEST_CODE = 788
    }

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: AuthPresenter

    private fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = ViewModelProvider(this, viewModelFactory).get(AuthPresenter::class.java)

        signInText.text = fromHtmlCompat(getString(R.string.sign_in_title))

        val errorTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onClearLoginError()
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        loginField.addTextChangedListener(errorTextWatcher)
        passwordField.addTextChangedListener(errorTextWatcher)

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

        close.setOnClickListener { finish() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if ((item?.itemId ?: -1) == android.R.id.home) {
            hideSoftKeyboard()
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

    private fun onClearLoginError() {
        loginButton.isEnabled = true
        authForm.isEnabled = true
        loginErrorMessage.visibility = View.GONE
    }

    private fun tryLogin() {
        val login = loginField.text.toString()
        val password = passwordField.text.toString()

        presenter.authWithLoginPassword(login, password)
    }

    override fun onSuccess() {
        setResult(RESULT_OK)
        analytics.logAmplitudeEvent(AmplitudeAnalytics.Auth.LOGGED_ID,
                mapOf(AmplitudeAnalytics.Auth.PARAM_SOURCE to AmplitudeAnalytics.Auth.VALUE_SOURCE_EMAIL))
        finish()
    }

    override fun onError(authError: AuthError) {
        @StringRes val messageResId = when (authError) {
            AuthError.ConnectionProblem     -> R.string.auth_error_connectivity
            AuthError.EmailPasswordInvalid  -> R.string.auth_error_email_password_invalid
            AuthError.TooManyAttempts       -> R.string.auth_error_too_many_attempts
        }

        if (authError == AuthError.EmailPasswordInvalid) {
            authForm.isEnabled = false
            loginButton.isEnabled = false
        }

        loginErrorMessage.setText(messageResId)
        loginErrorMessage.changeVisibillity(true)

        hideProgressDialogFragment(PROGRESS)
    }

    override fun onLoading() =
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_in), getString(R.string.processing_your_request))
}