package org.stepik.android.adaptive.ui.activity;


import android.content.res.Resources;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.stepik.android.adaptive.R;
import org.stepik.android.adaptive.ui.view.SwipeableLayout;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StudyActivityScreenshots {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Rule
    public ActivityTestRule<StudyActivity> mActivityTestRule = new ActivityTestRule<>(StudyActivity.class);

    @Test
    public void plainScreenshot() throws Exception {
        Thread.sleep(5000);

        Screengrab.screenshot("01");

        onView(withId(R.id.fragment_container)).perform(swipeLeft());

        Screengrab.screenshot("02");

        Thread.sleep(2000);
        onView(withId(R.id.fragment_container)).perform(swipeRight());

        Screengrab.screenshot("03");
        Thread.sleep(2000);

    }

    public static ViewAction swipeLeft() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                view -> {
                    float[] coordinates =  GeneralLocation.CENTER.calculateCoordinates(view);
                    coordinates[0] -= Resources.getSystem().getDisplayMetrics().widthPixels / 4;
                    return coordinates;
                }, Press.THUMB);
    }

    public static ViewAction swipeRight() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                view -> {
                    float[] coordinates =  GeneralLocation.CENTER.calculateCoordinates(view);
                    coordinates[0] += Resources.getSystem().getDisplayMetrics().widthPixels / 4;
                    return coordinates;
                }, Press.THUMB);
    }

}
