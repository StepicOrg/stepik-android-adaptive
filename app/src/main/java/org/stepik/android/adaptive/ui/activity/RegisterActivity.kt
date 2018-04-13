package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_register.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.RegisterPresenter
import org.stepik.android.adaptive.core.presenter.contracts.RegisterView
import org.stepik.android.adaptive.util.*
import javax.inject.Inject
import javax.inject.Provider

class RegisterActivity: BasePresenterActivity<RegisterPresenter, RegisterView>(), RegisterView {
    companion object {
        private const val PROGRESS = "register_progress"
    }

    @Inject
    lateinit var registerPresenterProvider: Provider<RegisterPresenter>

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        signUpText.text = fromHtmlCompat(getString(R.string.sign_up_title))

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = fromHtmlCompat(getString(R.string.terms_message_register)).stripUnderlinesFromLinks()

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
    }

    private fun register() {
        hideSoftKeyboard()

        val firstName = firstNameField.text.toString().trim()
        val lastName = secondNameField.text.toString().trim()

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()

        if (!ValidateUtil.isEmailValid(email)) {
//            return onError(getString(R.string.auth_error_empty_email))
        }

        presenter?.register(firstName, lastName, email, password)
    }

    override fun setState(state: RegisterView.State) = when (state) {
        is RegisterView.State.Idle -> {
            // reset view state
        }

        is RegisterView.State.Loading -> {
            showProgressDialogFragment(PROGRESS, getString(R.string.sign_up), getString(R.string.processing_your_request))
        }

        is RegisterView.State.Error -> {
            hideProgressDialogFragment(PROGRESS)
        }

        is RegisterView.State.Success -> {
            hideProgressDialogFragment(PROGRESS)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        hideProgressDialogFragment(PROGRESS)
        super.onStop()
    }

    override fun getPresenterProvider()
            = registerPresenterProvider
}