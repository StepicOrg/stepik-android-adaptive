package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.DialogBodyDailyRewardsBinding
import org.stepik.android.adaptive.databinding.DialogDefaultBodyBinding
import org.stepik.android.adaptive.gamification.DailyRewardManager
import org.stepik.android.adaptive.ui.adapter.DailyRewardsAdapter
import org.stepik.android.adaptive.ui.fragment.RecommendationsFragment

class DailyRewardDialog : DialogFragment() {
    companion object {
        private const val REWARD_PROGRESS_KEY = "reward_progress"

        fun newInstance(progress: Long): DailyRewardDialog {
            val dialog = DailyRewardDialog()
            val args = Bundle()
            args.putLong(REWARD_PROGRESS_KEY, progress)
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var binding: DialogDefaultBodyBinding
    private lateinit var discreteScrollView: DiscreteScrollView

    private val adapter = DailyRewardsAdapter(DailyRewardManager.rewards)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(requireContext(), R.style.ExpLevelDialogTheme)
        binding = DialogDefaultBodyBinding.inflate(requireActivity().layoutInflater, null, false)

        binding.title.text = getString(R.string.daily_reward_title)
        binding.description.text = getString(R.string.daily_reward_description)
        binding.continueButton.text = getString(R.string.daily_reward_button)

        binding.continueButton.setOnClickListener { getReward() }

        adapter.currentProgress = arguments?.getLong(REWARD_PROGRESS_KEY, 0)?.toInt() ?: 0

        discreteScrollView = DialogBodyDailyRewardsBinding.inflate(requireActivity().layoutInflater, binding.container, false).discreteScrollView
        discreteScrollView.adapter = adapter
        discreteScrollView.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.BOTTOM)
                .build()
        )
        discreteScrollView.scrollToPosition(adapter.currentProgress)

        binding.container.addView(discreteScrollView)

        alertDialogBuilder.setView(binding.root)

        return alertDialogBuilder.create()
    }

    private fun getReward() {
        parentFragment?.childFragmentManager?.let {
            InventoryDialog().show(it, RecommendationsFragment.INVENTORY_DIALOG_TAG)
        }
        dismiss()
    }
}
