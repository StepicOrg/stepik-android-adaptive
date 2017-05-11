package org.stepik.android.adaptive.pdd.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.stepik.android.adaptive.pdd.api.login.LoginListener;
import org.stepik.android.adaptive.pdd.api.login.SocialManager;
import org.stepik.android.adaptive.pdd.ui.fragment.LoginFragment;


public class LaunchActivity extends FragmentActivity {
    private LoginListener loginListener;
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 159;

    public void setLoginListener(final LoginListener listener) {
        this.loginListener = listener;
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "requestCode-" + requestCode + " resultCode-" + resultCode);
        if (loginListener != null) {
            if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    loginListener.onSocialLogin(res.accessToken, SocialManager.SocialType.vk);
                }

                @Override
                public void onError(VKError error) {
                    loginListener.onError(null);
                }
            })) {
                return;
            }

            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
                Log.d(getClass().getCanonicalName(), "REQUEST_CODE_GOOGLE_SIGN_IN");
                final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                Log.d(getClass().getCanonicalName(), "success: " + result.isSuccess());
                if (result.isSuccess()) {
                    final GoogleSignInAccount account = result.getSignInAccount();
                    if (account == null) {
                        loginListener.onError(null);
                    } else {
                        final String token = account.getServerAuthCode();
                        if (token == null) {
                            loginListener.onError(null);
                        } else {
                            loginListener.onSocialLogin(token, SocialManager.SocialType.google);
                        }
                    }
                } else {
                    loginListener.onError(null);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
