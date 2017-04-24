package org.stepik.droid.adaptive.pdd.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public final class QuizCardLayout extends FrameLayout {
    private OnTouchListener dispatchTouchListener;

    public QuizCardLayout(Context context) {
        super(context);
    }

    public QuizCardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuizCardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptHoverEvent(final MotionEvent event) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (dispatchTouchListener != null) {
            dispatchTouchListener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnDispatchTouchListener(final OnTouchListener listener) {
        this.dispatchTouchListener = listener;
    }
}
