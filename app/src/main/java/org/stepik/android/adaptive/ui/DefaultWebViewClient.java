package org.stepik.android.adaptive.ui;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.stepik.android.adaptive.ui.listener.OnPageFinishedListener;
import org.stepik.android.adaptive.ui.listener.ShouldOverrideUrlLoadingListener;

public class DefaultWebViewClient extends WebViewClient {
    private final OnPageFinishedListener pageFinishedListener;
    private final ShouldOverrideUrlLoadingListener shouldOverrideUrlLoadingListener;

    public DefaultWebViewClient(final OnPageFinishedListener pageFinishedListener) {
        this(null, pageFinishedListener);
    }

    public DefaultWebViewClient(final ShouldOverrideUrlLoadingListener shouldOverrideUrlLoadingListener) {
        this(shouldOverrideUrlLoadingListener, null);
    }

    public DefaultWebViewClient(final ShouldOverrideUrlLoadingListener shouldOverrideUrlLoadingListener,
                                final OnPageFinishedListener pageFinishedListener) {
        this.shouldOverrideUrlLoadingListener = shouldOverrideUrlLoadingListener;
        this.pageFinishedListener = pageFinishedListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return shouldOverrideUrlLoadingListener != null && shouldOverrideUrlLoadingListener.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (pageFinishedListener != null) {
            pageFinishedListener.onPageFinished(view, url);
        }
    }
}
