package org.stepik.droid.adaptive.pdd.ui.activity;

import android.support.v4.app.Fragment;

import org.stepik.droid.adaptive.pdd.ui.fragment.LoginFragment;


public class LaunchActivity extends FragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
