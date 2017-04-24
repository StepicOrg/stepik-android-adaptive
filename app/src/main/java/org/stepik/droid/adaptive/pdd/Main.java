package org.stepik.droid.adaptive.pdd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.stepik.droid.adaptive.pdd.ui.fragment.FragmentMgr;
import org.stepik.droid.adaptive.pdd.ui.fragment.LoginFragment;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        Util.initMgr(this);
        FragmentMgr.getInstance().attach(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentMgr.getInstance().addFragment(0, new LoginFragment(), false);
        }
    }
}
