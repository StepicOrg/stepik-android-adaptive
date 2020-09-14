package org.stepik.android.adaptive.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import kotlinx.android.synthetic.main.popup_window.view.*
import org.stepik.android.adaptive.R

object PopupHelper {
    private fun calcArrowHorizontalOffset(anchorView: View, popupView: View, arrowView: View): Float {
        val pos = IntArray(2)
        anchorView.getLocationOnScreen(pos)
        val anchorOffset = pos[0] + anchorView.measuredWidth / 2

        popupView.getLocationOnScreen(pos)
        return anchorOffset.toFloat() - pos[0] - arrowView.measuredWidth / 2
    }


    fun showPopupAnchoredToView(context: Context, anchorView: View?, popupText: String, x: Int = 0, y: Int = 0): PopupWindow? {
        if (anchorView == null) {
            return null
        }

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window, null)

        popupView.popupText.text = popupText

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                popupView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                popupView.arrowView.x = calcArrowHorizontalOffset(anchorView, popupView, popupView.arrowView)
            }
        }
        popupView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.animationStyle = R.style.PopupAnimations

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }

        anchorView.post {
            PopupWindowCompat.showAsDropDown(popupWindow, anchorView, x, y, Gravity.CENTER)
        }

        return popupWindow
    }
}