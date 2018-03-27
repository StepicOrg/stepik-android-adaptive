package org.stepik.android.adaptive.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.api.RegistrationResponse
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.LoginPresenter
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.databinding.ActivityRegisterBinding
import org.stepik.android.adaptive.util.ValidateUtil
import javax.inject.Inject
import javax.inject.Provider

class RegisterActivity : BasePresenterActivity<LoginPresenter, LoginView>(), LoginView {
    private companion object {
        private const val PROGRESS = "register_progress"
    }

    private lateinit var binding : ActivityRegisterBinding

    @Inject
    lateinit var loginPresenterProvider: Provider<LoginPresenter>

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        setSupportActionBar(binding.registerActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) when(v.id) {
                R.id.register_activity_first_name   -> binding.registerActivityFirstNameWrapper.isErrorEnabled  = false
                R.id.register_activity_second_name  -> binding.registerActivitySecondNameWrapper.isErrorEnabled = false
                R.id.register_activity_email        -> binding.registerActivityEmailWrapper.isErrorEnabled      = false
                R.id.register_activity_password     -> binding.registerActivityPasswordWrapper.isErrorEnabled   = false
            }
        }

        binding.registerActivityFirstName.onFocusChangeListener = focusChangeListener
        binding.registerActivitySecondName.onFocusChangeListener = focusChangeListener
        binding.registerActivityEmail.onFocusChangeListener = focusChangeListener
        binding.registerActivityPassword.onFocusChangeListener = focusChangeListener

        binding.registerActivityCreateAccount.setOnClickListener { createAccount() }

        binding.registerActivityTerms.movementMethod = LinkMovementMethod.getInstance()
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

    override fun onDestroy() {
        binding.unbind()
        super.onDestroy()
    }

    private fun createAccount() {
        val firstName = binding.registerActivityFirstName.text.toString().trim()
        val secondName = binding.registerActivitySecondName.text.toString().trim()
        val email = binding.registerActivityEmail.text.toString().trim()
        val password = binding.registerActivityPassword.text.toString()

        var isOk = true

        isOk = isOk && ValidateUtil.validateRequiredField(binding.registerActivityFirstNameWrapper, binding.registerActivityFirstName)
        isOk = isOk && ValidateUtil.validateRequiredField(binding.registerActivitySecondNameWrapper, binding.registerActivitySecondName)
        isOk = isOk && ValidateUtil.validateEmail(binding.registerActivityEmailWrapper, binding.registerActivityEmail)
        isOk = isOk && ValidateUtil.validatePassword(binding.registerActivityPasswordWrapper, binding.registerActivityPassword)

        if (isOk) {
            presenter?.createAccount(AccountCredentials(firstName, secondName, email, password))
        }
    }

    override fun onSuccess() {
        ScreenManager.getInstance().startStudy()
    }

    override fun onNetworkError() {
        hideProgressDialogFragment(PROGRESS)
        Snackbar.make(binding.root, R.string.register_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onError(errorBody: String) {
        hideProgressDialogFragment(PROGRESS)

        try {
            val error = Gson().fromJson(errorBody, RegistrationResponse::class.java)
            onFieldError(error.first_name, binding.registerActivityFirstNameWrapper)
            onFieldError(error.last_name, binding.registerActivitySecondNameWrapper)
            onFieldError(error.email, binding.registerActivityEmailWrapper)
            onFieldError(error.password, binding.registerActivityPasswordWrapper)
        } catch (e: Exception) {}
    }

    private fun onFieldError(msg: Array<String>?, wrapper: TextInputLayout) {
        val error = msg?.joinToString(" ") ?: ""
        if (error.isNotBlank()) {
            wrapper.error = error
        }
    }

    override fun onLoading() {
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_up), getString(R.string.processing_your_request))
    }

    override fun getPresenterProvider() = loginPresenterProvider
}