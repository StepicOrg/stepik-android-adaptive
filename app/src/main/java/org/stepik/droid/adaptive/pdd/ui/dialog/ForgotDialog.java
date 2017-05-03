package org.stepik.droid.adaptive.pdd.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.stepik.droid.adaptive.pdd.R;


public final class ForgotDialog extends DialogFragment implements Dialog.OnClickListener {
    private TextInputEditText editText;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setTitle(R.string.forgot_password);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, this);
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, this);
        alertDialogBuilder.setCancelable(false);

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_forgot, null);
        alertDialogBuilder.setView(view);

        editText = (TextInputEditText) view.findViewById(R.id.dialog_forgot_email);

        return alertDialogBuilder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case -1:
                Log.d("ForgotDialog", editText.getText().toString());
            break;
            case -2:
                dialogInterface.cancel();
            break;
        }
    }
}
