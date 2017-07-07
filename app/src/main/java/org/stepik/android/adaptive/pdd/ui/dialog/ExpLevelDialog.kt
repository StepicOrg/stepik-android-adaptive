package org.stepik.android.adaptive.pdd.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepik.android.adaptive.pdd.R

class ExpLevelDialog : DialogFragment() {
    companion object {
        private val LEVEL_KEY = "level"

        fun newInstance(level: Long) : ExpLevelDialog {
            val dialog = ExpLevelDialog()
            val args = Bundle()
            args.putLong(LEVEL_KEY, level)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
//        alertDialogBuilder.setTitle(String.format(getString(R.string.exp_level_title), arguments[LEVEL_KEY]))
//        alertDialogBuilder.setMessage(R.string.logout_dialog)

        alertDialogBuilder.setView(R.layout.exp_level_dialog)
        return alertDialogBuilder.create()
    }
}