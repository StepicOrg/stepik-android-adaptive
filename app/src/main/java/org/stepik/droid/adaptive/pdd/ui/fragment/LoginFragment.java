package org.stepik.droid.adaptive.pdd.ui.fragment;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.Util;
import org.stepik.droid.adaptive.pdd.api.API;
import org.stepik.droid.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.databinding.FragmentLoginBinding;
import org.stepik.droid.adaptive.pdd.ui.dialog.ForgotDialog;

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


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        setUIState(state);
        binding.fragmentLoginCreateAccount.setOnClickListener((v) ->
                FragmentMgr.getInstance().replaceFragment(0, new RegistrationFragment(), true));

        binding.fragmentLoginForgotPassword.setOnClickListener((v) ->
                FragmentMgr.getInstance().showDialog(new ForgotDialog()));


        binding.fragmentLoginButtonSignIn.setOnClickListener((v) -> authWithLoginPassword());

        binding.fragmentLoginEmail.addTextChangedListener(new FormTextWatcher(this::validateEmail));
        binding.fragmentLoginPassword.addTextChangedListener(new FormTextWatcher(this::validatePassword));

        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        }).observeOn(AndroidSchedulers.mainThread());

        compositeDisposable.add(resolveAuth
                .subscribeOn(Schedulers.io())
                .subscribe(this::setAuthState, e -> setAuthState(AuthState.ERROR)));
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
            Util.startStudy(getActivity());
        } else if (binding != null) {
            setUIState(state);
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
        } // todo handle error
    }

    private enum AuthState {
        OK,
        ERROR,
        NEED_AUTH,
        PENDING
    }

    private boolean validateEmail() {
        final String email = binding.fragmentLoginEmail.getText().toString().trim();
        final boolean valid = !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (valid) {
            binding.fragmentLoginEmailLayout.setErrorEnabled(false);
        } else {
            binding.fragmentLoginEmail.setError(getString(R.string.auth_error_empty_email));
        }
        return valid;
    }

    private boolean validatePassword() {
        final String password = binding.fragmentLoginPassword.getText().toString().trim();
        final boolean valid = !TextUtils.isEmpty(password);
        if (valid) {
            binding.fragmentLoginPasswordLayout.setErrorEnabled(false);
        } else {
            binding.fragmentLoginPassword.setError(getString(R.string.auth_error_empty_password));
        }
        return valid;
    }

    private boolean validateLoginForm() {
        return validateEmail() && validatePassword();
    }

    private void authWithLoginPassword() {
        if (!validateLoginForm()) return;

        authProgress = ProgressDialog.show(getContext(), getString(R.string.auth), getString(R.string.processing_your_request));

        final String email = binding.fragmentLoginEmail.getText().toString().trim();
        final String password = binding.fragmentLoginPassword.getText().toString().trim();

        compositeDisposable.add(API.getInstance()
            .initServices(API.TokenType.PASSWORD)
            .authWithLoginPassword(email, password)
            .subscribeOn(Schedulers.io())
            .doOnNext(API.getInstance()::updateAuthState)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((r) -> compositeDisposable.add(
                API.getInstance().getProfile()
                .doOnNext(profileResponse -> SharedPreferenceMgr.getInstance().saveProfile(profileResponse.getProfile()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profileResponse -> setAuthState(AuthState.OK), this::onLoginError)), this::onLoginError));
    }

    private void onLoginError(final Throwable throwable) {
        setAuthState(AuthState.ERROR);
        if (binding != null) {
            Snackbar.make(binding.getRoot(), R.string.auth_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private static final class FormTextWatcher implements TextWatcher {
        private final Runnable runnable;

        private FormTextWatcher(final Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            runnable.run();
        }
    }
}
