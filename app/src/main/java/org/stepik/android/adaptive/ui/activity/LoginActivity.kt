package org.stepik.android.adaptive.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.LoginPresenter
import org.stepik.android.adaptive.core.presenter.PresenterFactory
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.databinding.ActivityLoginBinding
import org.stepik.android.adaptive.ui.dialog.RemindPasswordDialog
import org.stepik.android.adaptive.util.ValidateUtil

class LoginActivity : BasePresenterActivity<LoginPresenter, LoginView>(), LoginView {
    private companion object {
        private const val PROGRESS = "login_progress"
        private const val REMIND_PASSWORD_DIALOG = "remind_password_dialog"
    }

    private var presenter: LoginPresenter? = null

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        setSupportActionBar(binding.loginActivityToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) when(v.id) {
                R.id.login_activity_email        -> binding.loginActivityEmailWrapper.isErrorEnabled      = false
                R.id.login_activity_password     -> binding.loginActivityPasswordWrapper.isErrorEnabled   = false
            }
        }

        binding.loginActivityEmail.onFocusChangeListener = focusChangeListener
        binding.loginActivityPassword.onFocusChangeListener = focusChangeListener

        binding.loginActivityTerms.movementMethod = LinkMovementMethod.getInstance()

        binding.loginActivitySignIn.setOnClickListener { authWithLoginPassword() }

        binding.loginActivityRemindPassword.setOnClickListener {
            RemindPasswordDialog().show(supportFragmentManager, REMIND_PASSWORD_DIALOG)
        }
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

    fun authWithLoginPassword() {
        val email = binding.loginActivityEmail.text.toString().trim()
        val password = binding.loginActivityPassword.text.toString()

        var isOk = true

        isOk = isOk && ValidateUtil.validateEmail(binding.loginActivityEmailWrapper, binding.loginActivityEmail)
        isOk = isOk && ValidateUtil.validatePassword(binding.loginActivityPasswordWrapper, binding.loginActivityPassword)

        if (isOk) {
            presenter?.authWithLoginPassword(email, password)
        }
    }


    override fun onSuccess() {
        ScreenManager.getInstance().startStudy()
    }

    override fun onNetworkError() {
        hideProgressDialogFragment(PROGRESS)
        Snackbar.make(binding.root, R.string.auth_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onError(errorBody: String) {
        hideProgressDialogFragment(PROGRESS)
    }

    override fun onLoading() {
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_in), getString(R.string.processing_your_request))
    }

    override fun onPresenter(presenter: LoginPresenter) {
        this.presenter = presenter
    }

    override fun getPresenterFactory(): PresenterFactory<LoginPresenter> = LoginPresenter.Companion
}