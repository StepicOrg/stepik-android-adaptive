package org.stepik.android.adaptive.pdd.ui.helper;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.stepik.android.adaptive.pdd.ui.view.CardWebView;

public final class LayoutHelper {

    private static final int CARD_SHADOW_PADDING = 32;

    public static final int MIN_CARD_HEIGHT_DP = 160;


    public static int pxFromDp(final Context context, final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setRelativeLayoutMarginTop(final RelativeLayout layout, final int marginTop) {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = marginTop;
        layout.setLayoutParams(params);
    }

    public static void setRelativeLayoutHeight(final ViewGroup layout, final int height) {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.height = height;
        layout.setLayoutParams(params);
    }

    public static void setCenterInParent(final ViewGroup view, final boolean value) {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (value) {
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        } else {
            params.removeRule(RelativeLayout.CENTER_VERTICAL);
        }
        view.setLayoutParams(params);
    }

    /**
     * Wraps webView with parent
     * @param parent - parent of webView
     * @param webView - webView to wrap
     */
    public static void wrapWebView(final ViewGroup parent, final CardWebView webView, final int offset) {
        int height = webView.getContentHeight();
        final int minHeight = LayoutHelper.pxFromDp(parent.getContext(), MIN_CARD_HEIGHT_DP);

        LayoutHelper.setCenterInParent(webView, height < minHeight);
        height = Math.max(height, minHeight);

        LayoutHelper.setRelativeLayoutHeight(
                parent,
                height
                        + LayoutHelper.pxFromDp(parent.getContext(), CARD_SHADOW_PADDING)
                        + LayoutHelper.pxFromDp(parent.getContext(), offset));
    }
}
