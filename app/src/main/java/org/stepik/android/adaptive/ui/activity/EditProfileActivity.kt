package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterActivity
import org.stepik.android.adaptive.core.presenter.EditProfilePresenter
import org.stepik.android.adaptive.core.presenter.contracts.EditProfileView
import org.stepik.android.adaptive.util.fromHtmlCompat
import javax.inject.Inject
import javax.inject.Provider

class EditProfileActivity: BasePresenterActivity<EditProfilePresenter, EditProfileView>(), EditProfileView {
    companion object {
        const val IS_FAKE_REGISTER_MODE = "is_fake_register_mode"
    }

    @Inject
    lateinit var editProfilePresenterProvider: Provider<EditProfilePresenter>

    override fun injectComponent() {
        App.componentManager().loginComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        signUpText.text = fromHtmlCompat(getString(R.string.sign_up_title))
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