package org.stepik.android.adaptive.pdd.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.ui.fragment.FragmentMgr;

public abstract class FragmentActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentMgr.getInstance().attach(this);

        setContentView(R.layout.fragment_activity);

        if (savedInstanceState == null) {
            FragmentMgr.getInstance().addFragment(0, createFragment(), false);
        }
    }

    protected abstract Fragment createFragment();

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
