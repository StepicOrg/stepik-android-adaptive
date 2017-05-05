package org.stepik.droid.adaptive.pdd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.data.SharedPreferenceMgr;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_title_1),
                getString(R.string.intro_description_1),
                R.drawable.ic_car,
                ContextCompat.getColor(this, R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_title_2),
                getString(R.string.intro_description_2),
                R.drawable.ic_intro_hard,
                ContextCompat.getColor(this, R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_title_3),
                getString(R.string.intro_description_3),
                R.drawable.ic_intro_easy,
                ContextCompat.getColor(this, R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_title_4),
                getString(R.string.intro_description_4),
                R.drawable.ic_done,
                ContextCompat.getColor(this, R.color.colorAccent)));

        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Completable
            .fromRunnable(() -> SharedPreferenceMgr.getInstance().setNotFirstTime(true))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                startActivity(new Intent(this, StudyActivity.class));
                finish();
            });
    }
}
