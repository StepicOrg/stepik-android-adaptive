package org.stepik.android.adaptive.ui.activity;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.stepik.android.adaptive.R;
import org.stepik.android.adaptive.Util;
import org.stepik.android.adaptive.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.data.db.DataBaseHelper;
import org.stepik.android.adaptive.data.db.DataBaseMgr;
import org.stepik.android.adaptive.util.AchievementManager;
import org.stepik.android.adaptive.util.DailyRewardManager;
import org.stepik.android.adaptive.util.ExpUtil;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StatsActivityScreenshots {

    private static final long GOOD_RATING = 128;

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
        if (ExpUtil.getExp() < GOOD_RATING) {
            mockStats();
            AchievementManager.INSTANCE.destroy();
        }
        DailyRewardManager.INSTANCE.giveRewardAndGetCurrentRewardDay();
    }

    private static void mockStats() {
        for (int i = 0; i < 10; i++) {
            final SQLiteDatabase db = DataBaseMgr.getInstance().getDb();
            final int val = Util.getRandomNumberBetween(10, 50);
            db.execSQL("INSERT INTO " + DataBaseHelper.Companion.getTABLE_EXP()
                    + " VALUES (" + val + ", date('now','-" + i + " day'), "
                    + Util.getRandomNumberBetween(Integer.MIN_VALUE, Integer.MAX_VALUE) + ");");
        }
        SharedPreferenceMgr.getInstance().saveLong("exp_key", DataBaseMgr.getInstance().getExp());
    }

    @Rule
    public ActivityTestRule<StatsActivity> mActivityTestRule = new ActivityTestRule<>(StatsActivity.class);

    @Test
    public void plainScreenshot() throws Exception {
        Thread.sleep(2000);

        try { // another attempt to close launcher crash dialog
            onView(withText("alert_dialog_text")).perform(pressBack());
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        onView(withId(R.id.coordinator))
                .perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.BOTTOM_LEFT, Press.FINGER));
        Screengrab.screenshot("05");
    }

}
