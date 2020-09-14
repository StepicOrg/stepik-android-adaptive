package org.stepik.android.adaptive.util;

import android.text.TextUtils;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.stepik.android.adaptive.R;

public class ValidateUtil {
    public static boolean validateRequiredField(final TextInputLayout layout, final TextInputEditText editText) {
        final String value = editText.getText().toString().trim();
        final boolean valid = !TextUtils.isEmpty(value);
        if (valid) {
            layout.setErrorEnabled(false);
        } else {
            layout.setError(layout.getContext().getString(R.string.required_field));
        }
        return valid;
    }

    public static boolean isEmailValid(final String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validateEmail(final TextInputLayout layout, final TextInputEditText editText) {
        final String email = editText.getText().toString().trim();
        final boolean valid = isEmailValid(email);
        if (valid) {
            layout.setErrorEnabled(false);
        } else {
            layout.setError(layout.getContext().getString(R.string.auth_error_empty_email));
        }
        return valid;
    }

    public static boolean validatePassword(final TextInputLayout layout, final TextInputEditText editText) {
        return validateRequiredField(layout, editText);
    }
}
