package org.stepik.android.adaptive.ui.listener;


import android.webkit.WebView;

public interface ShouldOverrideUrlLoadingListener {
    boolean shouldOverrideUrlLoading(WebView v, String url);
}
