package org.stepik.android.adaptive.pdd.ui.listener;


import android.webkit.WebView;

public interface ShouldOverrideUrlLoadingListener {
    boolean shouldOverrideUrlLoading(WebView v, String url);
}
