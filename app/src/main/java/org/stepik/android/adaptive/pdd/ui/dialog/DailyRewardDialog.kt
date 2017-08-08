package org.stepik.android.adaptive.pdd.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.DialogBodyDailyRewardsBinding
import org.stepik.android.adaptive.pdd.databinding.DialogDefaultBodyBinding
import org.stepik.android.adaptive.pdd.ui.adapter.DailyRewardsAdapter
import org.stepik.android.adaptive.pdd.ui.fragment.CardsFragment
import org.stepik.android.adaptive.pdd.util.DailyRewardManager

class DailyRewardDialog : DialogFragment() {
    companion object {
        private val REWARD_PROGRESS_KEY = "reward_progress"

        fun newInstance(progress: Long) : DailyRewardDialog {
            val dialog = DailyRewardDialog()
            dialog.arguments = Bundle()
            dialog.arguments.putLong(REWARD_PROGRESS_KEY, progress)
            return dialog
        }
    }

    private lateinit var binding: DialogDefaultBodyBinding
    private lateinit var discreteScrollView: DiscreteScrollView

    private val adapter = DailyRewardsAdapter(DailyRewardManager.rewards)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DialogDefaultBodyBinding.inflate(activity.layoutInflater, null, false)

        binding.title.text = getString(R.string.daily_reward_title)
        binding.description.text = getString(R.string.daily_reward_description)
        binding.continueButton.text = getString(R.string.daily_reward_button)

        binding.continueButton.setOnClickListener { getReward() }

        adapter.currentProgress = arguments?.getLong(REWARD_PROGRESS_KEY, 0)?.toInt() ?: 0

        discreteScrollView = DialogBodyDailyRewardsBinding.inflate(activity.layoutInflater, binding.container, false).discreteScrollView
        discreteScrollView.adapter = adapter
        discreteScrollView.setItemTransformer(ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.BOTTOM)
                .build())
        discreteScrollView.scrollToPosition(adapter.currentProgress)

        binding.container.addView(discreteScrollView)

        alertDialogBuilder.setView(binding.root)

        return alertDialogBuilder.create()
    }

    private fun getReward() {
        parentFragment?.childFragmentManager?.let {
            InventoryDialog().show(it, CardsFragment.INVENTORY_DIALOG_TAG)
        }
        dismiss()
    }
}