package org.stepik.android.adaptive.pdd.ui.dialog

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.DialogStreakRestoreBinding

class StreakRestoreDialog : DialogFragment() {
    companion object {
        private val STREAK_KEY = "streak"

        fun newInstance(streak: Long) : StreakRestoreDialog {
            val dialog = StreakRestoreDialog()
            dialog.arguments = Bundle()
            dialog.arguments.putLong(STREAK_KEY, streak)
            return dialog
        }
    }

    private lateinit var binding : DialogStreakRestoreBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DataBindingUtil.inflate(activity.layoutInflater, R.layout.dialog_streak_restore, null, false)



        alertDialogBuilder.setView(binding.root)
        return alertDialogBuilder.create()
    }


}