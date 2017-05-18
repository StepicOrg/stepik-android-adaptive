package org.stepik.android.adaptive.pdd.ui.helper;

import android.content.res.Resources;
import android.support.annotation.Px;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.stepik.android.adaptive.pdd.ui.view.CardWebView;

public final class LayoutHelper {
    public static final int P_8DP  = pxFromDp(8);
    public static final int P_16DP = pxFromDp(16);
    public static final int P_24DP = pxFromDp(24);
    public static final int P_32DP = pxFromDp(32);

    private static final int CARD_SHADOW_PADDING = P_32DP;

    private static final int MIN_CARD_HEIGHT = pxFromDp(160);


    public static int pxFromDp(final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
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

    /**
     * Wraps webView with parent
     * @param parent - parent of webView
     * @param webView - webView to wrap
     * @param offset - additional offset from bottom in px
     */
    public static void wrapWebView(final ViewGroup parent, final CardWebView webView, @Px final int offset) {
        final int height = webView.getContentHeight();

        if (height < MIN_CARD_HEIGHT) {
            parent.setPadding(P_16DP, P_8DP  + P_24DP,
                    P_16DP, P_24DP + P_24DP + offset);

            LayoutHelper.setRelativeLayoutHeight(parent,
                    height + P_24DP + P_24DP + CARD_SHADOW_PADDING + offset);
        } else {
            parent.setPadding(P_16DP, P_8DP, P_16DP, P_24DP);

            LayoutHelper.setRelativeLayoutHeight(parent,
                    height + CARD_SHADOW_PADDING + offset);
        }
    }
}
