package org.stepik.android.adaptive.ui.activity;

import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.stepik.android.adaptive.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class OnboardingTest {

    @Rule
    public ActivityTestRule<IntroActivity> mActivityTestRule = new ActivityTestRule<>(IntroActivity.class);

    @Test
    public void plainScreenshot() throws Exception {
        Thread.sleep(10000);

        onView(withId(R.id.fragment_container))
                .perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.BOTTOM_LEFT, Press.FINGER));
    }

}
