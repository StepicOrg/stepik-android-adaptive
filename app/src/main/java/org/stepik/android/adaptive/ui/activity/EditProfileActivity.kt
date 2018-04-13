package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.EditProfilePresenter
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileView
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.fromHtmlCompat
import org.stepik.android.adaptive.util.setOnKeyboardOpenListener
import org.stepik.android.adaptive.util.stripUnderlinesFromLinks
import javax.inject.Inject
import javax.inject.Provider

class EditProfileActivity: BasePresenterActivity<EditProfilePresenter, EditProfileView>(), EditProfileView {
    companion object {
        const val IS_FAKE_REGISTER_MODE = "is_fake_register_mode"
    }

    private val isFakeRegisterMode by lazy { intent?.getBooleanExtra(IS_FAKE_REGISTER_MODE, false) ?: false }

    @Inject
    lateinit var editProfilePresenterProvider: Provider<EditProfilePresenter>

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        signUpText.text = fromHtmlCompat(getString(R.string.sign_up_title))

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = fromHtmlCompat(getString(R.string.terms_message_register)).stripUnderlinesFromLinks()
        termsPrivacyRegisterTextView.changeVisibillity(isFakeRegisterMode)

        firstNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                secondNameField.requestFocus()
                true
            } else {
                false
            }
        }

        secondNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                emailField.requestFocus()
                true
            } else {
                false
            }
        }

        emailField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordField.requestFocus()
                true
            } else {
                false
            }
        }

        editProfileView.requestFocus()

        setOnKeyboardOpenListener(root_view, {
            signUpText.changeVisibillity(false)
        }, {
            signUpText.changeVisibillity(true)
        })

        close.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = editProfilePresenterProvider
}