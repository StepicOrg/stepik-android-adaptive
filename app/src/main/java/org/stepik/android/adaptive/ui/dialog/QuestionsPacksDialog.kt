package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_questions_packs.view.*
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.AnalyticMgr
import org.stepik.android.adaptive.data.AnalyticMgr.EVENT_ON_QUESTIONS_DIALOG_ACTION_CLICKED
import org.stepik.android.adaptive.data.AnalyticMgr.EVENT_ON_QUESTIONS_DIALOG_SHOWN

class QuestionsPacksDialog : DialogFragment() {
    companion object {
        fun newInstance() = QuestionsPacksDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState == null) {
            AnalyticMgr.getInstance().logEvent(EVENT_ON_QUESTIONS_DIALOG_SHOWN)
        }

        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        val root = activity.layoutInflater.inflate(R.layout.dialog_questions_packs, null, false)

        root.actionButton.setOnClickListener {
            AnalyticMgr.getInstance().logEvent(EVENT_ON_QUESTIONS_DIALOG_ACTION_CLICKED)
            ScreenManager.showQuestionsPacksScreen(activity)
            dismiss()
        }

        alertDialogBuilder.setView(root)
        return alertDialogBuilder.create()
    }
}