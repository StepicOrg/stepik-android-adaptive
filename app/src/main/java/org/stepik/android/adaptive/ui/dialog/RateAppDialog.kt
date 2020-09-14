package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.databinding.RateAppDialogBinding
import org.stepik.android.adaptive.util.RateAppManager
import javax.inject.Inject

class RateAppDialog : DialogFragment() {
    companion object {
        private const val RATING_COUNT_KEY = "rating"
        private const val RATING_ENABLED_KEY = "rating_enabled"
        private const val MIN_POSITIVE = 4

        fun newInstance() = RateAppDialog()
    }

    private lateinit var binding: RateAppDialogBinding

    @Inject
    lateinit var rateAppManager: RateAppManager

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.rate_app_title)

        binding = DataBindingUtil.inflate(requireActivity().layoutInflater, R.layout.rate_app_dialog, null, false)

        binding.starsContainer.setIsIndicator(savedInstanceState?.getBoolean(RATING_ENABLED_KEY) ?: false)
        binding.starsContainer.rating = (savedInstanceState?.getInt(RATING_COUNT_KEY) ?: 0).toFloat()
        binding.starsContainer.setOnRatingBarChangeListener { _, _, _ -> refresh() }

        binding.ok.isEnabled = binding.starsContainer.rating > 0

        binding.ok.setOnClickListener {
            binding.starsContainer.setIsIndicator(true)
            refresh()
            analytics.rate(binding.starsContainer.rating.toInt())
        }

        binding.later.setOnClickListener {
            if (binding.starsContainer.rating >= MIN_POSITIVE) {
                analytics.ratePositiveLater()
            } else {
                analytics.rateNegativeLater()
            }
            rateAppManager.onCloseLater()
            dismiss()
        }

        if (binding.starsContainer.isIndicator) {
            refresh()
        }

        binding.sendEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_negative_title))
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_negative_title)))
            intent.type = "message/rfc822"

            val mailer = Intent.createChooser(intent, null)
            try {
                startActivity(mailer)
            } catch (e: ActivityNotFoundException) {}

            analytics.rateNegativeEmail()
            rateAppManager.onCloseNegative()
            dismiss()
        }

        binding.openGooglePlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.data = Uri.parse("market://details?id=${requireContext().packageName}")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                intent.data = Uri.parse("http://play.google.com/store/apps/details?id=${requireContext().packageName}")
                startActivity(intent)
            }
            analytics.ratePositiveGooglePlay()
            rateAppManager.onRated()
            dismiss()
        }

        builder.setView(binding.root)

        val dg = builder.create()
        dg.setCancelable(false)
        dg.setCanceledOnTouchOutside(false)
        return dg
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(RATING_COUNT_KEY, binding.starsContainer.rating.toInt())
        outState.putBoolean(RATING_ENABLED_KEY, binding.starsContainer.isIndicator)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        analytics.rateCanceled()
        rateAppManager.onCloseLater()
        super.onDismiss(dialog)
    }

    private fun refresh() {
        binding.ok.isEnabled = binding.starsContainer.rating > 0

        if (binding.starsContainer.isIndicator) {
            binding.message.visibility = View.GONE
            binding.starsContainer.visibility = View.GONE
            binding.ok.visibility = View.GONE
            binding.feedbackText.visibility = View.VISIBLE
            binding.later.visibility = View.VISIBLE

            if (binding.starsContainer.rating >= MIN_POSITIVE) {
                binding.openGooglePlay.visibility = View.VISIBLE
                binding.feedbackText.setText(R.string.feedback_positive)
            } else {
                binding.sendEmail.visibility = View.VISIBLE
                binding.feedbackText.setText(R.string.feedback_negative)
            }
        }
    }
}