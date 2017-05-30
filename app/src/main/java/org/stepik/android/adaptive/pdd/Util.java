package org.stepik.android.adaptive.pdd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.vk.sdk.VKSdk;

import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.data.AnalyticMgr;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.ui.activity.StudyActivity;
import org.stepik.android.adaptive.pdd.ui.activity.IntroActivity;
import org.stepik.android.adaptive.pdd.ui.fragment.FragmentMgr;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Util {
    public static void initMgr(final Context context) {
        Config.init(context);
        API.init();
        FragmentMgr.init();
        SharedPreferenceMgr.init(context);
        VKSdk.initialize(context);
        AnalyticMgr.init(context);
    }



    public static void hideSoftKeyboard(final Activity a) {
        final View view = a.getCurrentFocus();
        if (view != null) {
            Log.d("hideSoftKeyboard", "hideSoftKeyboard");
            final InputMethodManager mgr = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void startStudy(final Activity activity) {
        Observable.fromCallable(SharedPreferenceMgr.getInstance()::isNotFirstTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((notFirstTime) -> {
                    final Intent intent = new Intent(activity, notFirstTime ? StudyActivity.class : IntroActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                });
    }

    public static boolean checkPlayServices(final Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static int getRandomNumberBetween(final int lower, final int upper) {
        return (int) (Math.random() * (upper - lower)) + lower;
    }
}
