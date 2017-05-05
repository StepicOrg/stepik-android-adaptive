package org.stepik.droid.adaptive.pdd.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.stepik.droid.adaptive.pdd.api.login.LoginListener;
import org.stepik.droid.adaptive.pdd.api.login.SocialManager;
import org.stepik.droid.adaptive.pdd.ui.fragment.LoginFragment;


public class LaunchActivity extends FragmentActivity {
    private LoginListener loginListener;

    public void setLoginListener(final LoginListener listener) {
        this.loginListener = listener;
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                if (loginListener != null) loginListener.onSocialLogin(res.accessToken, SocialManager.SocialType.vk);
            }

            @Override
            public void onError(VKError error) {
                if (loginListener != null) loginListener.onError(null);
            }
        })) {
           return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
