package org.stepik.android.adaptive.pdd.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;


public final class CardWebView extends WebView {
    public CardWebView(Context context) {
        super(context);
    }

    public CardWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getContentHeight() {
        return super.computeVerticalScrollRange();
    }
}
