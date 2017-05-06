package org.stepik.droid.adaptive.pdd.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.stepik.droid.adaptive.pdd.R;
import org.stepik.droid.adaptive.pdd.api.API;
import org.stepik.droid.adaptive.pdd.ui.activity.LaunchActivity;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
        if (which == -1) {
            Completable
                    .fromRunnable(() -> API.getInstance().updateAuthState(null))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        getContext().startActivity(new Intent(getContext(), LaunchActivity.class));
                        getActivity().finish();
                    });
        }
    }
}
