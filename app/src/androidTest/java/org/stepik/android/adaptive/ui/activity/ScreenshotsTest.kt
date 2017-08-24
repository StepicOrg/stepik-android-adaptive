package org.stepik.android.adaptive.ui.activity

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.test.suitebuilder.annotation.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.ViewAction
import org.stepik.android.adaptive.R
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.*
import android.support.test.espresso.matcher.ViewMatchers.*

import android.support.test.espresso.Espresso.onView

@LargeTest
@RunWith(AndroidJUnit4::class)
class ScreenshotsTest {

    @Rule
    var mActivityTestRule = ActivityTestRule(StudyActivity::class.java)

    @Test
    fun ScreenshotsTest() {
        try {
            Thread.sleep(2000)
        } catch (e: Exception) {

        }
        onView(withId(R.id.fragment_container)).perform(swipeUp())
    }

    private fun swipeUp() = GeneralSwipeAction(Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER)
}