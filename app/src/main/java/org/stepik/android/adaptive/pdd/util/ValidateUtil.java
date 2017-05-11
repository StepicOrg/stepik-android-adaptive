package org.stepik.android.adaptive.pdd.util;


import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import org.stepik.android.adaptive.pdd.R;

public class ValidateUtil {

    public static boolean validateEmail(final TextInputLayout layout, final TextInputEditText editText) {
        final String email = editText.getText().toString().trim();
        final boolean valid = !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (valid) {
            layout.setErrorEnabled(false);
        } else {
            layout.setError(layout.getContext().getString(R.string.auth_error_empty_email));
        }
        return valid;
    }

    public static boolean validatePassword(final TextInputLayout layout, final TextInputEditText editText) {
        final String password = editText.getText().toString().trim();
        final boolean valid = !TextUtils.isEmpty(password);
        if (valid) {
            layout.setErrorEnabled(false);
        } else {
            layout.setError(layout.getContext().getString(R.string.auth_error_empty_password));
        }
        return valid;
    }

    public static final class FormTextWatcher implements TextWatcher {
        private final Runnable runnable;

        public FormTextWatcher(final Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            runnable.run();
        }
    }
}
