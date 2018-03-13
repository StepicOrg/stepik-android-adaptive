package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_questions_packs.view.*
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager

class QuestionsPacksDialog : DialogFragment() {
    companion object {
        fun newInstance() = QuestionsPacksDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        val root = activity.layoutInflater.inflate(R.layout.dialog_questions_packs, null, false)

        root.actionButton.setOnClickListener {
            ScreenManager.showQuestionsPacksScreen(activity)
        }

        alertDialogBuilder.setView(root)
        return alertDialogBuilder.create()
    }
}