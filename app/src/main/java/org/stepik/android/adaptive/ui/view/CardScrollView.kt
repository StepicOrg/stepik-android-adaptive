package org.stepik.android.adaptive.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CardScrollView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : ScrollView(context, attributeSet, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    fun canScrollVertically() =
        canScrollVertically(-1) || canScrollVertically(1)

    override fun onTouchEvent(ev: MotionEvent?) =
        if (ev?.action == MotionEvent.ACTION_DOWN)
            canScrollVertically()
        else
            super.onTouchEvent(ev)
}
