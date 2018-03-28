package org.stepik.android.adaptive.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.databinding.DialogStreakRestoreBinding
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment
import org.stepik.android.adaptive.gamification.InventoryManager
import javax.inject.Inject

class StreakRestoreDialog : DialogFragment() {
    companion object {
        private const val STREAK_KEY = "streak"

        fun newInstance(streak: Long) : StreakRestoreDialog {
            val dialog = StreakRestoreDialog()
            dialog.arguments = Bundle()
            dialog.arguments.putLong(STREAK_KEY, streak)
            return dialog
        }
    }

    private lateinit var binding : DialogStreakRestoreBinding

    @Inject
    lateinit var inventoryManager: InventoryManager

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState == null) {
            analytics.onStreakRestoreDialogShown()
        }

        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DialogStreakRestoreBinding.inflate(activity.layoutInflater, null, false)

        binding.ticketItem.counter.text = getString(R.string.amount, inventoryManager.getItemsCount(InventoryManager.Item.Ticket))

        binding.useCouponButton.setOnClickListener {
            if (inventoryManager.useItem(InventoryManager.Item.Ticket)) {
                binding.ticketItem.counter.text = getString(R.string.amount, inventoryManager.getItemsCount(InventoryManager.Item.Ticket))
                onStreakRestore()
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            analytics.onStreakRestoreCanceled(arguments?.getLong(STREAK_KEY) ?: 0)
            dismiss()
        }

        alertDialogBuilder.setView(binding.root)

        return alertDialogBuilder.create()
    }

    private fun onStreakRestore() {
        val streak = arguments?.getLong(STREAK_KEY) ?: 0
        analytics.onStreakRestored(streak)
        val intent = Intent()
        intent.putExtra(RecommendationsFragment.STREAK_RESTORE_KEY, streak)
        parentFragment?.onActivityResult(RecommendationsFragment.STREAK_RESTORE_REQUEST_CODE, Activity.RESULT_OK, intent) // used parentFragment instead of targetFragment due to bug on screen orientation change
    }
}