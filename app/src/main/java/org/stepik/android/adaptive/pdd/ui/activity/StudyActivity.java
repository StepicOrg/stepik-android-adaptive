package org.stepik.android.adaptive.pdd.ui.activity;

import android.support.v4.app.Fragment;

import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment;

public class StudyActivity extends FragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CardsFragment();
    }
}
