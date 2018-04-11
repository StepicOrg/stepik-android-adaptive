package org.stepik.android.adaptive.ui.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.api.Api
import org.stepik.android.adaptive.api.auth.SocialManager
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.LoginPresenter
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.databinding.ActivityLaunchBinding
import javax.inject.Inject
import javax.inject.Provider

class LaunchActivity: BasePresenterActivity<LoginPresenter, LoginView>(), LoginView {
    companion object {
        const val REQUEST_CODE_GOOGLE_SIGN_IN = 159
        const val REQUEST_CODE_SOCIAL_AUTH = 231

        private const val PROGRESS = "launch_progress"
    }

    private var googleApiClient : GoogleApiClient? = null
    private lateinit var callbackManager : CallbackManager

    private lateinit var binding : ActivityLaunchBinding

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var loginPresenterProvider: Provider<LoginPresenter>

    @Inject
    lateinit var screenManager: ScreenManager

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_launch)

        binding.launchActivitySignIn.setOnClickListener { showLoginScreen() }
        binding.launchActivityCreateAccount.setOnClickListener { showRegisterScreen() }

        binding.launchActivityVkButton.setOnClickListener { VKSdk.login(this, VKScopes.EMAIL) }

        if (Util.checkPlayServices(this)) {
            val googleSignInOptions =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Scope(Scopes.EMAIL), Scope(Scopes.PROFILE))
                    .requestServerAuthCode(config.googleServerClientId)
                    .build()

            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this, { presenter?.onError() })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .addApi(Auth.CREDENTIALS_API)
                    .build()

            binding.launchActivityGoogleButton.setOnClickListener {
                startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), REQUEST_CODE_GOOGLE_SIGN_IN)
            }
        } else {
            binding.launchActivityGoogleButton.isEnabled = false
        }

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {}

            override fun onSuccess(loginResult: LoginResult?) {
                if (loginResult != null) {
                    presenter?.onSocialLogin(loginResult.accessToken.token, SocialManager.SocialType.facebook)
                }
            }

            override fun onError(p0: FacebookException?) {
               presenter?.onError()
            }

        })

        binding.launchActivityFbButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this@LaunchActivity, listOf("email"))
        }

        binding.launchActivityTwitterButton.setOnClickListener { onSocialAuth(SocialManager.SocialType.twitter) }
        binding.launchActivityMailruButton.setOnClickListener { onSocialAuth(SocialManager.SocialType.mailru) }
        binding.launchActivityGithubButton.setOnClickListener { onSocialAuth(SocialManager.SocialType.github) }
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun showRegisterScreen() {
        startActivity(Intent(this, RegisterActivity::class.java))
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

    override fun onSaveInstanceState(outState: Bundle?) {} // caused FC on low android versions

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

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (resultCode == RESULT_OK && result.isSuccess) {
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

        if (requestCode == REQUEST_CODE_SOCIAL_AUTH) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    presenter?.authWithCode(it.data.getQueryParameter(config.codeQueryParameter))
                }
            } else {
                presenter?.onError()
            }
        }
    }

    private fun onSocialAuth(type: SocialManager.SocialType) {
        val intent = Intent(this, SocialAuthActivity::class.java)
        intent.data = api.getUriForSocialAuth(type)
        startActivityForResult(intent, REQUEST_CODE_SOCIAL_AUTH)
    }

    override fun onSuccess() {
        screenManager.startStudy()
    }

    override fun onNetworkError() {
        hideProgressDialogFragment(PROGRESS)
        Snackbar.make(binding.root, R.string.auth_error, Snackbar.LENGTH_LONG).show()
    }

    override fun onError(errorBody: String) {}

    override fun onLoading() {
        showProgressDialogFragment(PROGRESS, getString(R.string.sign_in), getString(R.string.processing_your_request))
    }

    override fun getPresenterProvider() = loginPresenterProvider
}