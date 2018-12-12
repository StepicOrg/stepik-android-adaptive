package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_register.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.RegisterPresenter
import org.stepik.android.adaptive.core.presenter.contracts.RegisterView
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.util.*
import javax.inject.Inject
import javax.inject.Provider

class RegisterActivity: BasePresenterActivity<RegisterPresenter, RegisterView>(), RegisterView {
    companion object {
        private const val PROGRESS = "register_progress"

        const val REQUEST_CODE = 520
    }

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var registerPresenterProvider: Provider<RegisterPresenter>

    @Inject
    lateinit var screenManager: ScreenManager

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        signUpText.text = fromHtmlCompat(getString(R.string.sign_up_title))

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = fromHtmlCompat(getString(R.string.terms_message_register)).stripUnderlinesFromLinks()

        val formWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                onClearError()
                setSignUpButtonState()
            }
        }

        firstNameField.addTextChangedListener(formWatcher)
        secondNameField.addTextChangedListener(formWatcher)
        emailField.addTextChangedListener(formWatcher)
        passwordField.addTextChangedListener(formWatcher)

        firstNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                secondNameField.requestFocus()
                true
            } else {
                false
            }
        }

        secondNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                emailField.requestFocus()
                true
            } else {
                false
            }
        }

        emailField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordField.requestFocus()
                true
            } else {
                false
            }
        }

        registerView.requestFocus()

        setOnKeyboardOpenListener(root_view, {
            signUpText.changeVisibillity(false)
        }, {
            signUpText.changeVisibillity(true)
        })

        close.setOnClickListener { finish() }

        signUpButton.setOnClickListener { register() }
        setSignUpButtonState()
    }

    private fun register() {
        hideSoftKeyboard()

        val firstName = firstNameField.text.toString().trim()
        val lastName = secondNameField.text.toString().trim()

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()

        presenter?.register(firstName, lastName, email, password)
    }

    override fun setState(state: RegisterView.State) = when (state) {
        is RegisterView.State.Idle -> {
            // reset view state
        }

        is RegisterView.State.Loading ->
            showProgressDialogFragment(PROGRESS, getString(R.string.sign_up), getString(R.string.processing_your_request))

        is RegisterView.State.NetworkError ->
            onError(getString(R.string.connectivity_error))

        is RegisterView.State.EmptyEmailError ->
            onChangesNeededError(getString(R.string.auth_error_empty_email))

        is RegisterView.State.Error ->
            onChangesNeededError(state.message)

        is RegisterView.State.Success -> {
            hideProgressDialogFragment(PROGRESS)
            onSuccess()
        }
    }

    private fun onChangesNeededError(message: String) {
        signUpButton.isEnabled = false
        onError(message)
    }

    private fun onError(message: String) {
        hideProgressDialogFragment(PROGRESS)

        registerForm.isEnabled = false
        registerErrorMessage.text = message
        registerErrorMessage.changeVisibillity(true)
    }

    private fun onClearError() {
        signUpButton.isEnabled = true
        registerForm.isEnabled = true
        registerErrorMessage.changeVisibillity(false)
    }

    private fun onSuccess() {
        setResult(RESULT_OK)
        analytics.logAmplitudeEvent(AmplitudeAnalytics.Auth.REGISTERED,
                mapOf(AmplitudeAnalytics.Auth.PARAM_SOURCE to AmplitudeAnalytics.Auth.VALUE_SOURCE_EMAIL))
        finish()
    }

    private fun setSignUpButtonState() {
        signUpButton.isEnabled = !emailField.text.isNullOrBlank() && !firstNameField.text.isNullOrBlank() && !passwordField.text.isNullOrBlank()
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider()
            = registerPresenterProvider
}