package org.stepik.droid.adaptive.pdd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import org.stepik.droid.adaptive.pdd.ui.fragment.FragmentMgr;
import org.stepik.droid.adaptive.pdd.ui.fragment.LoginFragment;

public class Main extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.initMgr(this);
        FragmentMgr.getInstance().attach(this);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentMgr.getInstance().addFragment(0, new LoginFragment(), false);
        }
    }
}
