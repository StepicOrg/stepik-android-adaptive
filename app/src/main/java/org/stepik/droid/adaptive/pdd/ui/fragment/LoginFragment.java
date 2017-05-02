package org.stepik.droid.adaptive.pdd.ui.fragment;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.api.API;
import org.stepik.droid.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.droid.adaptive.pdd.databinding.FragmentLoginBinding;
import org.stepik.droid.adaptive.pdd.ui.DefaultWebViewClient;

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

    private WebViewClient defaultWevViewClient;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        binding.fragmentLoginButton.setOnClickListener((v) -> {
            binding.fragmentLoginWeb.setVisibility(View.VISIBLE);
            binding.fragmentLoginProgress.setVisibility(View.GONE);
            binding.fragmentLoginButton.setVisibility(View.GONE);
            binding.fragmentLoginWeb.loadUrl(API.getAuthURL());
            binding.fragmentLoginWeb.requestFocus(View.FOCUS_DOWN);
        });

        binding.fragmentLoginWeb.setWebViewClient(defaultWevViewClient);
        binding.fragmentLoginWeb.getSettings().setJavaScriptEnabled(true);

        setUIState(state);

//        if (savedInstanceState == null) {
//            CookieManager.getInstance().removeAllCookie();
//        }

        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        compositeDisposable = new CompositeDisposable();

        Observable<AuthState> resolveAuth = Observable.fromCallable(() -> {
            API.init();
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
                }
            } else {
                return AuthState.NEED_AUTH;
            }
            return AuthState.OK;
        }).observeOn(AndroidSchedulers.mainThread());

        compositeDisposable.add(resolveAuth
                .subscribeOn(Schedulers.io())
                .subscribe(this::setAuthState, e -> setAuthState(AuthState.ERROR)));

        defaultWevViewClient = new DefaultWebViewClient((v, url) -> {
            if (url.startsWith(API.REDIRECT_URI)) {
                if (url.contains("success")) {
                    final Uri uri = Uri.parse(url);
                    binding.fragmentLoginWeb.setVisibility(View.GONE);
                    binding.fragmentLoginButton.setVisibility(View.GONE);

                    compositeDisposable.add(API.getInstance().authWithCode(uri.getQueryParameter(API.AUTH_CODE))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(API.getInstance()::updateAuthState)
                            .subscribeOn(Schedulers.io())
                            .subscribe(res ->
                                compositeDisposable.add(API.getInstance().getProfile()
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext(profileResponse ->
                                                SharedPreferenceMgr.getInstance().saveProfile(profileResponse.getProfile()))
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(profileResponse -> setAuthState(AuthState.OK), e -> setAuthState(AuthState.ERROR))),
                            e -> setAuthState(AuthState.ERROR)));
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        binding.unbind();
        binding = null;

        compositeDisposable.dispose();
        super.onDestroyView();
    }

    private void setAuthState(final AuthState state) {
        this.state = state;

        if (state == AuthState.OK) {
            FragmentMgr.getInstance().replaceFragment(0, new RecommendationsFragment(), false);
        } else if (binding != null) {
            setUIState(state);
        }
    }

    private void setUIState(final AuthState state) {
        if (state == AuthState.PENDING) {
            binding.fragmentLoginProgress.setVisibility(View.VISIBLE);
            binding.fragmentLoginButton.setVisibility(View.GONE);
            binding.fragmentLoginWeb.setVisibility(View.GONE);
        } else if (state == AuthState.NEED_AUTH || state == AuthState.ERROR) {
            binding.fragmentLoginProgress.setVisibility(View.GONE);
            binding.fragmentLoginButton.setVisibility(View.VISIBLE);
            binding.fragmentLoginWeb.setVisibility(View.GONE);
        } // todo handle error
    }

    private enum AuthState {
        OK,
        ERROR,
        NEED_AUTH,
        PENDING
    }
}
