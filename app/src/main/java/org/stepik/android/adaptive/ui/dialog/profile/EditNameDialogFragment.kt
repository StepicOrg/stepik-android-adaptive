package org.stepik.android.adaptive.ui.dialog.profile

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.dialog_edit_name.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterDialogFragment
import org.stepik.android.adaptive.core.presenter.EditProfileFieldPresenter
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileFieldView
import org.stepik.android.adaptive.ui.fragment.ProfileFragment.Companion.PROFILE_CHANGED_REQUEST_CODE
import org.stepik.android.adaptive.util.ValidateUtil
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.hideAllChildren
import javax.inject.Inject
import javax.inject.Provider

class EditNameDialogFragment: BasePresenterDialogFragment<EditProfileFieldPresenter, EditProfileFieldView>(), EditProfileFieldView {
    @Inject
    lateinit var editProfileFieldPresenterProvider: Provider<EditProfileFieldPresenter>

    override fun injectComponent() {
        App.componentManager()
                .statsComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_edit_name, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        confirm.setOnClickListener { changeName() }
        setConfirmButton()

        val formWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                onClearError()
                setConfirmButton()
            }
        }

        firstName.addTextChangedListener(formWatcher)
        secondName.addTextChangedListener(formWatcher)

        cancel.setOnClickListener { dismiss() }
    }

    private fun syncName() {
        presenter?.syncName(firstName.text.toString(), secondName.text.toString())
    }

    private fun changeName() {
        val isValid = ValidateUtil.validateRequiredField(firstNameWrapper, firstName) &&
                ValidateUtil.validateRequiredField(secondNameWrapper, secondName)

        if (!isValid) return

        presenter?.changeName(firstName.text.toString(), secondName.text.toString())
    }

    private fun onClearError() {
        firstNameWrapper.isErrorEnabled = false
        secondNameWrapper.isErrorEnabled = false
    }

    private fun setConfirmButton() {
        confirm.isEnabled = firstName.text.isNotBlank() && secondName.text.isNotBlank()
    }

    override fun setState(state: EditProfileFieldView.State) {
        container.hideAllChildren()

        when (state) {
            is EditProfileFieldView.State.Loading ->
                progress.changeVisibillity(true)

            is EditProfileFieldView.State.ProfileLoaded -> {
                profileForm.changeVisibillity(true)
                firstName.setText(state.profile.firstName)
                secondName.setText(state.profile.lastName)
            }

            is EditProfileFieldView.State.Success -> {
                parentFragment?.onActivityResult(PROFILE_CHANGED_REQUEST_CODE, RESULT_OK, null)
                dismiss()
            }

            is EditProfileFieldView.State.NameError -> {
                profileForm.changeVisibillity(true)
                onFieldError(state.error.firstName, firstNameWrapper)
                onFieldError(state.error.lastName, secondNameWrapper)
            }

            is EditProfileFieldView.State.NetworkError -> {
                profileForm.changeVisibillity(true)
                secondNameWrapper.error = getString(R.string.connectivity_error)
            }
        }
    }

    private fun onFieldError(msg: Array<String>?, wrapper: TextInputLayout) {
        val error = msg?.joinToString(" ") ?: ""
        if (error.isNotBlank()) {
            wrapper.error = error
        }
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        syncName()
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = editProfileFieldPresenterProvider
}