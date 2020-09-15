package org.stepik.android.adaptive.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.stepik.android.adaptive.App;
import org.stepik.android.adaptive.R;
import org.stepik.android.adaptive.api.Api;
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler;
import org.stepik.android.adaptive.di.qualifiers.MainScheduler;
import org.stepik.android.adaptive.util.ValidateUtil;

import javax.inject.Inject;

import io.reactivex.Scheduler;

public final class RemindPasswordDialog extends DialogFragment {
    private TextInputEditText editText;
    private TextInputLayout layout;

    private static final String PROGRESS = "progress";

    @Inject
    public Api api;

    @Inject
    @MainScheduler
    public Scheduler mainScheduler;

    @Inject
    @BackgroundScheduler
    public Scheduler backgroundScheduler;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.Companion.componentManager().getLoginComponent().inject(this);
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());

        alertDialogBuilder.setTitle(R.string.remind_password);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        layout = (TextInputLayout) layoutInflater.inflate(R.layout.dialog_remind_password, null);
        alertDialogBuilder.setView(layout);

        editText = layout.findViewById(R.id.dialog_remind_email);

        editText.setOnFocusChangeListener((view, b) -> {
            if (b) {
                layout.setErrorEnabled(false);
            }
        });

        final Dialog dialog = alertDialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(__ -> {
                if (validateEmail()) {
                    showProgressDialog();
                    api
                            .remindPassword(editText.getText().toString().trim())
                            .subscribeOn(backgroundScheduler)
                            .observeOn(mainScheduler)
                            .subscribe(this::onSuccess, this::onError);
                }
            });
        }
    }

    private boolean validateEmail() {
        return ValidateUtil.validateEmail(layout, editText);
    }

    private void showProgressDialog() {
        ProgressDialogFragment.Companion
                .newInstance(getString(R.string.remind_password), getString(R.string.processing_your_request))
                .show(getChildFragmentManager(), PROGRESS);
    }

    private void hideProgressDialog() {
        final Fragment fragment = getChildFragmentManager().findFragmentByTag(PROGRESS);
        if (fragment != null && fragment instanceof ProgressDialogFragment) {
            ((ProgressDialogFragment) fragment).dismiss();
        }
    }

    private void onSuccess() {
        Toast.makeText(getContext(), R.string.remind_password_success, Toast.LENGTH_SHORT).show();
        hideProgressDialog();
        dismiss();
    }

    private void onError(final Throwable throwable) {
        Toast.makeText(getContext(), R.string.request_error, Toast.LENGTH_SHORT).show();
        hideProgressDialog();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
