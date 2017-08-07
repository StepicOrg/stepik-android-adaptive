package org.stepik.android.adaptive.pdd.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.AnalyticMgr
import org.stepik.android.adaptive.pdd.databinding.DialogStreakRestoreBinding
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment
import org.stepik.android.adaptive.pdd.util.InventoryUtil

class StreakRestoreDialog : DialogFragment() {
    companion object {
        private val STREAK_KEY = "streak"

        fun newInstance(streak: Long) : StreakRestoreDialog {
            AnalyticMgr.getInstance().onStreakRestoreDialogShown()
            val dialog = StreakRestoreDialog()
            dialog.arguments = Bundle()
            dialog.arguments.putLong(STREAK_KEY, streak)
            return dialog
        }
    }

    private lateinit var binding : DialogStreakRestoreBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DialogStreakRestoreBinding.inflate(activity.layoutInflater, null, false)

        binding.ticketItem.counter.text = getString(R.string.amount, InventoryUtil.getItemsCount(InventoryUtil.Item.Ticket))

        binding.useCouponButton.setOnClickListener {
            if (InventoryUtil.useItem(InventoryUtil.Item.Ticket)) {
                binding.ticketItem.counter.text = getString(R.string.amount, InventoryUtil.getItemsCount(InventoryUtil.Item.Ticket))
                onStreakRestore()
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            AnalyticMgr.getInstance().onStreakRestoreCanceled(arguments?.getLong(STREAK_KEY) ?: 0)
            dismiss()
        }

        alertDialogBuilder.setView(binding.root)

        val dg = alertDialogBuilder.create()
        dg.setCancelable(false)
        dg.setCanceledOnTouchOutside(false)
        return dg
    }

    private fun onStreakRestore() {
        val streak = arguments?.getLong(STREAK_KEY) ?: 0
        AnalyticMgr.getInstance().onStreakRestored(streak)
        val intent = Intent()
        intent.putExtra(CardsFragment.STREAK_RESTORE_KEY, streak)
        parentFragment?.onActivityResult(CardsFragment.STREAK_RESTORE_REQUEST_CODE, Activity.RESULT_OK, intent) // used parentFragment instead of targetFragment due to bug on screen orientation change
    }
}