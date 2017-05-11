package org.stepik.android.adaptive.pdd.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.model.VKScopes;

import org.stepik.android.adaptive.pdd.Config;
import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.Util;
import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.login.LoginListener;
import org.stepik.android.adaptive.pdd.api.login.SocialManager;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.databinding.FragmentLoginBinding;
import org.stepik.android.adaptive.pdd.ui.activity.LaunchActivity;
import org.stepik.android.adaptive.pdd.ui.dialog.RemindPasswordDialog;
import org.stepik.android.adaptive.pdd.util.ValidateUtil;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public final class LoginFragment extends Fragment {
    private final static String TAG = "LoginFragment";

    private FragmentLoginBinding binding;
    private AuthState state = AuthState.PENDING;

    private CompositeDisposable compositeDisposable;

    private ProgressDialog authProgress;

    private GoogleApiClient googleApiClient;

    private final LoginListener loginListener;

    public LoginFragment() {
        loginListener = new LoginListener() {
            @Override
            public void onLogin(final OAuthResponse response) {
                API.getInstance()
                        .joinCourse(Config.getInstance().getCourseId())
                        .subscribeOn(Schedulers.io())
                        .subscribe();


                compositeDisposable.add(
                        API.getInstance().getProfile()
                                .doOnNext(profileResponse -> SharedPreferenceMgr.getInstance().saveProfile(profileResponse.getProfile()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(profileResponse -> setAuthState(AuthState.OK), this::onError)
                );
            }

            @Override
            public void onSocialLogin(String token, SocialManager.SocialType type) {
                showProgressDialog();
                super.onSocialLogin(token, type);
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                setAuthState(AuthState.ERROR);
                if (binding != null) {
                    Snackbar.make(binding.getRoot(), R.string.auth_error, Snackbar.LENGTH_LONG).show();
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        setUIState(state);
        binding.fragmentLoginCreateAccount.setOnClickListener((v) ->
                FragmentMgr.getInstance().replaceFragment(0, new RegistrationFragment(), true));

        binding.fragmentLoginRemindPassword.setOnClickListener((v) ->
                FragmentMgr.getInstance().showDialog(new RemindPasswordDialog()));


        binding.fragmentLoginButtonSignIn.setOnClickListener((v) -> authWithLoginPassword());

        binding.fragmentLoginEmail.addTextChangedListener(new ValidateUtil.FormTextWatcher(this::validateEmail));
        binding.fragmentLoginPassword.addTextChangedListener(new ValidateUtil.FormTextWatcher(this::validatePassword));

        binding.fragmentLoginVkButton.setOnClickListener((v) -> VKSdk.login(getActivity(), VKScopes.EMAIL));

        if (googleApiClient == null) {
            binding.fragmentLoginGoogleButton.setEnabled(false);
        } else {
            binding.fragmentLoginGoogleButton.setOnClickListener((v) ->
                    startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), LaunchActivity.REQUEST_CODE_GOOGLE_SIGN_IN));
        }

        return binding.getRoot();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        compositeDisposable = new CompositeDisposable();

        Observable<AuthState> resolveAuth = Observable.fromCallable(() -> {
            final OAuthResponse response = SharedPreferenceMgr.getInstance().getOAuthResponse();
            if (response != null) {
                final long expire = SharedPreferenceMgr.getInstance().getLong(SharedPreferenceMgr.OAUTH_RESPONSE_DEADLINE);
                if (System.currentTimeMillis() > expire) {
                    final Response<OAuthResponse> res = API.getInstance().authWithRefreshToken(response.getRefreshToken()).execute();
                    if (res.isSuccessful()) {
                        API.getInstance().updateAuthState(res.body());
                    } else {
                        return AuthState.ERROR;
                    }
                } else {
                    API.getInstance().updateAuthState(response);
                }
            } else {
                return AuthState.NEED_AUTH;
            }
            return AuthState.OK;
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());

        compositeDisposable.add(resolveAuth.subscribe(this::setAuthState, e -> setAuthState(AuthState.ERROR)));

        if (Util.checkPlayServices(getContext())) {
            final GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
                    .requestServerAuthCode(Config.getInstance().getGoogleServerClientId())
                    .build();

            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .enableAutoManage(getActivity(), (r) -> loginListener.onError(null))
                    .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                    .addApi(Auth.CREDENTIALS_API)
                    .build();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (activity instanceof LaunchActivity) {
            ((LaunchActivity) activity).setLoginListener(loginListener);
        }
    }

    @Override
    public void onDestroyView() {
        binding.unbind();
        binding = null;

        compositeDisposable.dispose();
        super.onDestroyView();
    }

    private void setAuthState(final AuthState state) {

        if (authProgress != null) authProgress.dismiss();

        if (state == AuthState.OK && this.state != state) { // to not to call it twice
            AnalyticMgr.getInstance().successLogin();
            Util.startStudy(getActivity());
        } else {
            if (state == AuthState.ERROR) {
                Completable.fromRunnable(() -> API.getInstance().updateAuthState(null)) // remove tokens if not authorized
                        .subscribeOn(Schedulers.io()).subscribe();
            }
            if (binding != null) {
                setUIState(state);
            }
        }
        this.state = state;
    }

    private void setUIState(final AuthState state) {
        if (state == AuthState.PENDING) {
            binding.fragmentLoginProgress.setVisibility(View.VISIBLE);
            binding.fragmentLoginMainScreen.setVisibility(View.GONE);
        } else if (state == AuthState.NEED_AUTH || state == AuthState.ERROR) {
            binding.fragmentLoginProgress.setVisibility(View.GONE);
            binding.fragmentLoginMainScreen.setVisibility(View.VISIBLE);
        }
    }

    private enum AuthState {
        OK,
        ERROR,
        NEED_AUTH,
        PENDING
    }

    private boolean validateEmail() {
        return ValidateUtil.validateEmail(binding.fragmentLoginEmailLayout, binding.fragmentLoginEmail);
    }

    private boolean validatePassword() {
        return ValidateUtil.validatePassword(binding.fragmentLoginPasswordLayout, binding.fragmentLoginPassword);
    }

    private boolean validateLoginForm() {
        return validateEmail() && validatePassword();
    }

    private void authWithLoginPassword() {
        if (!validateLoginForm()) return;

        showProgressDialog();

        final String email = binding.fragmentLoginEmail.getText().toString().trim();
        final String password = binding.fragmentLoginPassword.getText().toString().trim();

        compositeDisposable.add(API.getInstance()
            .authWithLoginPassword(email, password)
            .subscribeOn(Schedulers.io())
            .doOnNext(API.getInstance()::updateAuthState)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(loginListener::onLogin, loginListener::onError));
    }

    private void showProgressDialog() {
        authProgress = ProgressDialog.show(getContext(), getString(R.string.auth), getString(R.string.processing_your_request));
    }
}
