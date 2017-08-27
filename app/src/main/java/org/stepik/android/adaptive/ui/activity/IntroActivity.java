package org.stepik.android.adaptive.ui.activity;

import android.support.v4.app.Fragment;
import org.stepik.android.adaptive.ui.fragment.OnboardingFragment;

public class IntroActivity extends FragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new OnboardingFragment();
    }
}