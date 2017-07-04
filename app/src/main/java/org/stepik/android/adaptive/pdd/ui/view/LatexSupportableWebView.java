package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import java.util.Calendar;


public class LatexSupportableWebView extends WebView implements View.OnClickListener, View.OnTouchListener {
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    OnWebViewImageClicked listener;

    private final static String assetUrl = "file:///android_asset/";

    public LatexSupportableWebView(Context context) {
        this(context, null);
    }

    public LatexSupportableWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LatexSupportableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);
        setOnLongClickListener(v -> true);

        setOnClickListener(this);
        setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        WebView.HitTestResult hr = getHitTestResult();
        try {
            if (listener != null && hr.getType() == HitTestResult.IMAGE_TYPE) {
                listener.onClick(hr.getExtra());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    performClick();
                }
            }
        }
        return false;
    }

    public void setOnWebViewClickListener(OnWebViewImageClicked listener) {
        this.listener = listener;
    }

    public interface OnWebViewImageClicked {
        void onClick(String path);
    }
}
