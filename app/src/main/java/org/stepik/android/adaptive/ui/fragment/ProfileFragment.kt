package org.stepik.android.adaptive.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_auth.*
import kotlinx.android.synthetic.main.header_empty_auth.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.view_profile.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.core.presenter.ProfilePresenter
import org.stepik.android.adaptive.core.presenter.contracts.ProfileView
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.ui.activity.LoginActivity
import org.stepik.android.adaptive.ui.activity.RegisterActivity
import org.stepik.android.adaptive.ui.dialog.profile.EditEmailDialogFragment
import org.stepik.android.adaptive.ui.dialog.profile.EditNameDialogFragment
import org.stepik.android.adaptive.ui.dialog.profile.EditPasswordDialogFragment
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.fromHtmlCompat
import org.stepik.android.adaptive.util.hideAllChildren
import javax.inject.Inject
import javax.inject.Provider

class ProfileFragment: BasePresenterFragment<ProfilePresenter, ProfileView>(), ProfileView {
    companion object {
        const val EDIT_NAME_DIALOG = "edit_name"
        const val EDIT_EMAIL_DIALOG = "edit_email"
        const val EDIT_EMAIL_PASSWORD = "edit_password"

        const val PROFILE_CHANGED_REQUEST_CODE = 131
    }

    @Inject
    lateinit var profilePresenterProvider: Provider<ProfilePresenter>

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var analytics: Analytics

    override fun injectComponent() {
        App.componentManager()
                .statsComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        signIn.setOnClickListener {
            startActivityForResult(Intent(context, LoginActivity::class.java), LoginActivity.REQUEST_CODE)
            analytics.logEvent(Analytics.Login.SHOW_LOGIN_SCREEN_FROM_PROFILE)
        }
        signUp.setOnClickListener {
            startActivityForResult(Intent(context, RegisterActivity::class.java), RegisterActivity.REQUEST_CODE)
            analytics.logEvent(Analytics.Registration.SHOW_REGISTRATION_SCREEN_FROM_PROFILE)
        }
        close.changeVisibillity(false)

        description.text = fromHtmlCompat(getString(R.string.empty_auth_description))

        changeName.setOnClickListener {
            analytics.logEvent(Analytics.Profile.ON_CHANGE_NAME)
            showEditNameDialog()
        }
        changeEmail.setOnClickListener {
            analytics.logEvent(Analytics.Profile.ON_CHANGE_EMAIL)
            showEditEmailDialog()
        }
        changePassword.setOnClickListener {
            analytics.logEvent(Analytics.Profile.ON_CHANGE_PASS)
            showEditPasswordDialog()
        }
    }

    private fun showEditNameDialog() =
            EditNameDialogFragment().show(childFragmentManager, EDIT_NAME_DIALOG)

    private fun showEditEmailDialog() =
            EditEmailDialogFragment().show(childFragmentManager, EDIT_EMAIL_DIALOG)

    private fun showEditPasswordDialog() =
            EditPasswordDialogFragment().show(childFragmentManager, EDIT_EMAIL_PASSWORD)

    override fun setState(state: ProfileView.State) {
        profileContainer.hideAllChildren()
        when(state) {
            is ProfileView.State.EmptyAuth -> {
                emptyAuth.changeVisibillity(true)
            }

            is ProfileView.State.ProfileLoaded -> {
                profileView.changeVisibillity(true)
                profileName.text = state.profile.fullName

                val email = state.profile.emailAddressesResolved.firstOrNull()?.email
                profileMail.text = email
                profileMail.changeVisibillity(email != null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.fetchProfile()
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = profilePresenterProvider
}