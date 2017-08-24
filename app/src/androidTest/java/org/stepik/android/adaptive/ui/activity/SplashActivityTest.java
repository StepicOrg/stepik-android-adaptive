package org.stepik.android.adaptive.ui.activity;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SplashActivityTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Test
    public void splashActivityTest() {

        ViewInteraction appCompatButton = onView(
                allOf(withId(org.stepik.android.adaptive.R.id.next), withText("Дальше"),
                        withParent(withId(org.stepik.android.adaptive.R.id.supplemental_actions)),
                        isDisplayed()));
        appCompatButton.perform(click());


        ViewInteraction appCompatButton2 = onView(
                allOf(withId(org.stepik.android.adaptive.R.id.next), withText("Дальше"),
                        withParent(withId(org.stepik.android.adaptive.R.id.supplemental_actions)),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(org.stepik.android.adaptive.R.id.continue_button), withText("Получить награду"), isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(org.stepik.android.adaptive.R.id.continue_button), withText("Продолжить"), isDisplayed()));
        appCompatButton4.perform(click());

    }

}
