package org.stepik.android.adaptive.ui.activity;


import android.content.res.Resources;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swiper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.MotionEvent;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.stepik.android.adaptive.R;
import org.stepik.android.adaptive.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.util.ExpUtil;

import io.reactivex.functions.Action;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.*;

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
        final CardSwiper dragLeft = new CardSwiper(() -> {
            Screengrab.screenshot("02");
        });

        onView(withId(R.id.cards_container)).perform(dragCard(dragLeft, CardSwiper.Direction.Left));


        final CardSwiper dragRight = new CardSwiper(() -> {
            Screengrab.screenshot("03");
        });

        onView(withId(R.id.cards_container)).perform(dragCard(dragRight, CardSwiper.Direction.Right));
        Thread.sleep(1000);
    }

    private static class CardSwiper implements Swiper {
        private enum Direction {
            Left, Right
        }

        private final Action onDrag;

        private CardSwiper(Action onDrag) {
            this.onDrag = onDrag;
        }

        @Override
        public Status sendSwipe(UiController uiController, float[] startCoordinates, float[] endCoordinates, float[] precision) {
            MotionEvent down
                    = MotionEvents.sendDown(uiController, startCoordinates, precision).down;
            uiController.loopMainThreadForAtLeast(10);
            MotionEvents.sendMovement(uiController, down, endCoordinates);

            uiController.loopMainThreadForAtLeast(1500);
            try {
                onDrag.run();
                uiController.loopMainThreadForAtLeast(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MotionEvents.sendCancel(uiController, down);
            down.recycle();
            return Swiper.Status.SUCCESS;
        }
    }

    public static ViewAction dragCard(final Swiper swiper, final CardSwiper.Direction d) {
        return new GeneralSwipeAction(swiper,
                view -> {
                    float[] coordinates = GeneralLocation.CENTER.calculateCoordinates(view);
                    coordinates[1] += 150;
                    return coordinates;
                },
                view -> {
                    float[] coordinates = GeneralLocation.CENTER.calculateCoordinates(view);
                    coordinates[0] += (d == CardSwiper.Direction.Right ? 1 : -1) *
                            Resources.getSystem().getDisplayMetrics().widthPixels / 4;
                    coordinates[1] += 150;
                    return coordinates;
                }, Press.THUMB);
    }
}
