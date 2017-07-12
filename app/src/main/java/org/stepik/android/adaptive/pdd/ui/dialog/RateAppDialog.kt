package org.stepik.android.adaptive.pdd.ui.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.AnalyticMgr
import org.stepik.android.adaptive.pdd.databinding.RateAppDialogBinding
import org.stepik.android.adaptive.pdd.util.RateAppUtil

class RateAppDialog : DialogFragment() {
    companion object {
        private val RATING_COUNT_KEY = "rating"
        private val RATING_ENABLED_KEY = "rating_enabled"
    }

    private lateinit var binding: RateAppDialogBinding
    private lateinit var adapter: StarsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(R.string.rate_app_title)

        binding = DataBindingUtil.inflate(activity.layoutInflater, R.layout.rate_app_dialog, null, false)

        adapter = StarsAdapter(5, savedInstanceState?.getInt(RATING_COUNT_KEY) ?: -1, binding)
        adapter.enabled = savedInstanceState?.getBoolean(RATING_ENABLED_KEY) ?: true
        binding.ok.isEnabled = adapter.selected > -1

        binding.starsContainer.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.starsContainer.adapter = adapter

        binding.ok.setOnClickListener {
            adapter.enabled = false
            adapter.refresh()
            AnalyticMgr.getInstance().rate(adapter.selected + 1)
        }

        binding.later.setOnClickListener {
            if (adapter.selected >= StarsAdapter.MIN_POSITIVE) {
                AnalyticMgr.getInstance().ratePositiveLater()
            } else {
                AnalyticMgr.getInstance().rateNegativeLater()
            }
            RateAppUtil.onCloseLater()
            dismiss()
        }

        if (!adapter.enabled) {
            adapter.refresh()
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

            AnalyticMgr.getInstance().rateNegativeEmail()
            RateAppUtil.onCloseNegative()
            dismiss()
        }

        binding.openGooglePlay.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.data = Uri.parse("market://details?id=${context.packageName}")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                intent.data = Uri.parse("http://play.google.com/store/apps/details?id=${context.packageName}")
                startActivity(intent)
            }
            AnalyticMgr.getInstance().ratePositiveGooglePlay()
            RateAppUtil.onRated()
            dismiss()
        }

        builder.setView(binding.root)

        val dg = builder.create()
        dg.setCancelable(false)
        dg.setCanceledOnTouchOutside(false)
        return dg
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(RATING_COUNT_KEY, adapter.selected)
        outState?.putBoolean(RATING_ENABLED_KEY, adapter.enabled)
        super.onSaveInstanceState(outState)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        AnalyticMgr.getInstance().rateCanceled()
        super.onDismiss(dialog)
    }

    private class StarsAdapter(private val stars: Int, var selected: Int, val binding: RateAppDialogBinding) : RecyclerView.Adapter<StarsAdapter.StarViewHolder>() {
        companion object {
            val MIN_POSITIVE = 3
        }
        var enabled = true

        override fun onBindViewHolder(holder: StarViewHolder, position: Int) {
            val color = holder.view.context.resources.getColor(if (position <= selected) R.color.colorAccent else R.color.colorRadioButtonDefault)

            val imageDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(holder.view.context, R.drawable.ic_star)).mutate()
            DrawableCompat.setTint(imageDrawable, color)
            holder.view.setImageDrawable(imageDrawable)

            if (enabled) {
                holder.view.setOnClickListener {
                    selected = position
                    refresh()
                }
            } else {
                holder.view.isClickable = false
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
                StarViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.star_view, parent, false) as ImageView)

        override fun getItemCount() = stars

        fun refresh() {
            if (selected > -1) {
                binding.ok.isEnabled = true
            }
            if (!enabled) {
                binding.message.visibility = View.GONE
                binding.starsContainer.visibility = View.GONE
                binding.ok.visibility = View.GONE
                binding.feedbackText.visibility = View.VISIBLE
                binding.later.visibility = View.VISIBLE

                if (selected >= MIN_POSITIVE) {
                    binding.openGooglePlay.visibility = View.VISIBLE
                    binding.feedbackText.setText(R.string.feedback_positive)
                } else {
                    binding.sendEmail.visibility = View.VISIBLE
                    binding.feedbackText.setText(R.string.feedback_negative)
                }
            }
            notifyDataSetChanged()
        }

        class StarViewHolder(val view: ImageView) : RecyclerView.ViewHolder(view)
    }
}