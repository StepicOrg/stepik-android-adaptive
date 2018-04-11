package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.LogoutHelper
import org.stepik.android.adaptive.core.ScreenManager
import javax.inject.Inject

class LogoutDialog: DialogFragment(), DialogInterface.OnClickListener {
    @Inject
    lateinit var logoutHelper: LogoutHelper

    @Inject
    lateinit var screenManager: ScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.logout)
        alertDialogBuilder.setMessage(R.string.logout_dialog)
        alertDialogBuilder.setPositiveButton(android.R.string.yes, this)
        alertDialogBuilder.setNegativeButton(android.R.string.no, this)

        return alertDialogBuilder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            logoutHelper.logout(screenManager::showOnboardingScreen)
        }
    }
}