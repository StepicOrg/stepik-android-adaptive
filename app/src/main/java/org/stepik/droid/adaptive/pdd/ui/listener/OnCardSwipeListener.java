package org.stepik.droid.adaptive.pdd.ui.listener;



public interface OnCardSwipeListener {
    enum SWIPE_DIRECTION {
        LEFT, RIGHT
    }

    void onCardSwipe(SWIPE_DIRECTION direction);
}
