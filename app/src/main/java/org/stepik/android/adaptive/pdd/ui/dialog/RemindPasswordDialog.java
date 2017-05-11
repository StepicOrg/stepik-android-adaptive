package org.stepik.android.adaptive.pdd.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Toast;

import org.stepik.android.adaptive.pdd.R;
import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.util.ValidateUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public final class RemindPasswordDialog extends DialogFragment implements Dialog.OnClickListener {
    private TextInputEditText editText;
    private TextInputLayout layout;
    private ProgressDialog progressDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setTitle(R.string.remind_password);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, this);
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, this);

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        layout = (TextInputLayout) layoutInflater.inflate(R.layout.dialog_remind_password, null);
        alertDialogBuilder.setView(layout);

        editText = (TextInputEditText) layout.findViewById(R.id.dialog_remind_email);

        editText.addTextChangedListener(new ValidateUtil.FormTextWatcher(this::validateEmail));

        final Dialog dialog = alertDialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private boolean validateEmail() {
        return ValidateUtil.validateEmail(layout, editText);
    }

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(getContext(), getString(R.string.remind_password), getString(R.string.processing_your_request));
    }

    private void onSuccess() {
        Toast.makeText(progressDialog.getContext(), R.string.remind_password_success, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    private void onError(final Throwable throwable) {
        Toast.makeText(progressDialog.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case -1:
                if (validateEmail()) {
                    showProgressDialog();
                    API.getInstance()
                            .remindPassword(editText.getText().toString().trim())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::onSuccess, this::onError);
                }
            break;
            case -2:
                dialogInterface.dismiss();
            break;
        }
    }
}
