package org.stepik.android.adaptive.pdd.ui.activity;

import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment;
import org.stepik.android.adaptive.pdd.util.AchievementManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class StudyActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();

        Completable.fromRunnable(() -> {})
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    AchievementManager.INSTANCE.show((FrameLayout) findViewById(R.id.fragment_container));
                });
    }

    @Override
    protected Fragment createFragment() {
        return new CardsFragment();
    }
}
