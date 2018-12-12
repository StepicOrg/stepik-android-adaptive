package org.stepik.android.adaptive.ui.dialog.profile

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.dialog_edit_password.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterDialogFragment
import org.stepik.android.adaptive.core.presenter.EditProfileFieldPresenter
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileFieldView
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.ui.fragment.ProfileFragment
import org.stepik.android.adaptive.util.ValidateUtil
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.hideAllChildren
import javax.inject.Inject
import javax.inject.Provider

class EditPasswordDialogFragment: BasePresenterDialogFragment<EditProfileFieldPresenter, EditProfileFieldView>(), EditProfileFieldView {
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
            inflater.inflate(R.layout.dialog_edit_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        confirm.setOnClickListener { changePassword() }
        setConfirmButton()

        val formWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                onClearError()
                setConfirmButton()
            }
        }

        oldPassword.addTextChangedListener(formWatcher)
        newPassword.addTextChangedListener(formWatcher)

        cancel.setOnClickListener { dismiss() }
    }

    private fun changePassword() {
        val isValid = ValidateUtil.validatePassword(oldPasswordWrapper, oldPassword) &&
                ValidateUtil.validatePassword(newPasswordWrapper, newPassword)

        if (!isValid) return

        presenter?.changePassword(oldPassword.text.toString(), newPassword.text.toString())
    }

    private fun onClearError() {
        oldPasswordWrapper.isErrorEnabled = false
        newPasswordWrapper.isErrorEnabled = false
    }

    private fun setConfirmButton() {
        confirm.isEnabled = !oldPassword.text.isNullOrBlank() && !newPassword.text.isNullOrBlank()
    }

    override fun onProfile(profile: Profile) {
        // no op
    }

    override fun setState(state: EditProfileFieldView.State) {
        container.hideAllChildren()

        when (state) {
            is EditProfileFieldView.State.Loading ->
                progress.changeVisibillity(true)

            is EditProfileFieldView.State.ProfileLoaded ->
                profileForm.changeVisibillity(true)

            is EditProfileFieldView.State.Success -> {
                parentFragment?.onActivityResult(ProfileFragment.PROFILE_CHANGED_REQUEST_CODE, Activity.RESULT_OK, null)
                dismiss()
            }

            is EditProfileFieldView.State.PasswordError -> {
                profileForm.changeVisibillity(true)
                newPasswordWrapper.error = state.error.detail
            }

            is EditProfileFieldView.State.NetworkError -> {
                profileForm.changeVisibillity(true)
                newPasswordWrapper.error = getString(R.string.connectivity_error)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = editProfileFieldPresenterProvider
}