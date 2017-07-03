package org.stepik.android.adaptive.pdd.ui.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import com.vk.sdk.api.model.VKScopes
import org.stepik.android.adaptive.pdd.Config
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.api.login.SocialManager
import org.stepik.android.adaptive.pdd.core.ScreenManager
import org.stepik.android.adaptive.pdd.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.pdd.core.presenter.LoginPresenter
import org.stepik.android.adaptive.pdd.core.presenter.PresenterFactory
import org.stepik.android.adaptive.pdd.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.pdd.databinding.ActivityLaunchBinding

class LaunchActivity : BasePresenterActivity<LoginPresenter, LoginView>(), LoginView {
    companion object {
        val REQUEST_CODE_GOOGLE_SIGN_IN = 159
    }

    private val PROGRESS = "launch_progress"

    private var presenter: LoginPresenter? = null

    private var googleApiClient : GoogleApiClient? = null

    private lateinit var binding : ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_launch)

        binding.launchActivitySignIn.setOnClickListener { ScreenManager.getInstance().showLoginScreen() }
        binding.launchActivityCreateAccount.setOnClickListener { ScreenManager.getInstance().showRegisterScreen() }

        binding.launchActivityVkButton.setOnClickListener { VKSdk.login(this, VKScopes.EMAIL) }

        if (Util.checkPlayServices(this)) {
            val googleSignInOptions =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                    .requestServerAuthCode(Config.getInstance().googleServerClientId)
                    .build()

            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this, { presenter?.onError() })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .addApi(Auth.CREDENTIALS_API)
                    .build()

            binding.launchActivityGoogleButton.setOnClickListener {
                startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), LaunchActivity.REQUEST_CODE_GOOGLE_SIGN_IN)
            }
        } else {
            binding.launchActivityGoogleButton.isEnabled = false
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken?) {
                if (res != null) {
                    presenter?.onSocialLogin(res.accessToken, SocialManager.SocialType.vk)
                }
            }

            override fun onError(error: VKError?) {
                presenter?.onError()
            }
        })) {
            return
        }

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                if (account == null) {
                    presenter?.onError()
                } else {
                    val token = account.serverAuthCode
                    if (token != null) {
                        presenter?.onSocialLogin(token, SocialManager.SocialType.google)
                    } else {
                        presenter?.onError()
                    }
                }
            } else {
                presenter?.onError()
            }
        }
    }

    override fun onSuccess() {
        ScreenManager.getInstance().startStudy()
    }

    override fun onNetworkError() {
        hideProgressDialogFragment(PROGRESS)
        Snackbar.make(binding.root, R.string.auth_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onError(errorBody: String) {}

    override fun onLoading() {
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_in), getString(R.string.processing_your_request))
    }

    override fun onPresenter(presenter: LoginPresenter) {
        this.presenter = presenter
    }

    override fun getPresenterFactory(): PresenterFactory<LoginPresenter> = LoginPresenter.Companion
}