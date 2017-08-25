package org.stepik.android.adaptive.ui.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.stepik.android.adaptive.ui.fragment.OnboardingFragment;
import org.stepik.android.adaptive.util.DailyRewardManager;
import org.stepik.android.adaptive.util.ExpUtil;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

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
            ExpUtil.changeExp(GOOD_RATING, -1);
        }
        DailyRewardManager.INSTANCE.giveRewardAndGetCurrentRewardDay();
    }

    @Rule
    public ActivityTestRule<StatsActivity> mActivityTestRule = new ActivityTestRule<>(StatsActivity.class);

    @Test
    public void plainScreenshot() throws Exception {
        Thread.sleep(2000);
        Screengrab.screenshot("05");
    }

}
