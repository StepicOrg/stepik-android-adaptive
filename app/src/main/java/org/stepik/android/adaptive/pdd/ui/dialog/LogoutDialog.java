package org.stepik.android.adaptive.pdd.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.core.LogoutHelper;
import org.stepik.android.adaptive.pdd.core.ScreenManager;

public final class LogoutDialog extends DialogFragment implements Dialog.OnClickListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(R.string.logout);
        alertDialogBuilder.setMessage(R.string.logout_dialog);
        alertDialogBuilder.setPositiveButton(android.R.string.yes, this);
        alertDialogBuilder.setNegativeButton(android.R.string.no, this);

        return alertDialogBuilder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            LogoutHelper.logout(ScreenManager.getInstance()::showLaunchScreen);
        }
    }
}
