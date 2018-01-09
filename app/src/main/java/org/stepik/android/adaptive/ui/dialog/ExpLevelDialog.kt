package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.github.jinatonic.confetti.CommonConfetti
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ExpLevelDialogBinding
import java.util.concurrent.TimeUnit

class ExpLevelDialog : DialogFragment() {
    companion object {
        private const val LEVEL_KEY = "level"

        fun newInstance(level: Long) : ExpLevelDialog {
            val dialog = ExpLevelDialog()
            val args = Bundle()
            args.putLong(LEVEL_KEY, level)
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var binding : ExpLevelDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DataBindingUtil.inflate(activity.layoutInflater, R.layout.exp_level_dialog, null, false)
        binding.expLevelDialogTitle.text = arguments.getLong(LEVEL_KEY).toString()

        binding.continueButton.setOnClickListener { dismiss() }

        alertDialogBuilder.setView(binding.root)
        return alertDialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()
        Completable
                .timer(0, TimeUnit.MICROSECONDS) // js like work around
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
            CommonConfetti.rainingConfetti(binding.expLevelDialogConfetti, intArrayOf(
                    Color.BLACK,
                    ContextCompat.getColor(context, R.color.colorAccentDisabled),
                    ContextCompat.getColor(context, R.color.colorAccent)
            )).infinite().setVelocityY(100f, 30f).setVelocityX(0f, 60f).setEmissionRate(15f)
        }
    }
}