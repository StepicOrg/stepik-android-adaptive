package org.stepik.android.adaptive.pdd.ui.helper;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ViewGroup;


public final class LayoutHelper {
    public static int pxFromDp(final int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void setViewGroupPaddingTop(final ViewGroup layout, final int paddingTop) {
        layout.setPadding(layout.getPaddingLeft(), paddingTop, layout.getPaddingRight(), layout.getPaddingBottom());
    }
}
