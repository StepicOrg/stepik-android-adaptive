package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_auth.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.core.presenter.ProfilePresenter
import org.stepik.android.adaptive.core.presenter.contracts.ProfileView
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.hideAllChildren
import javax.inject.Inject
import javax.inject.Provider

class ProfileFragment: BasePresenterFragment<ProfilePresenter, ProfileView>(), ProfileView {
    @Inject
    lateinit var profilePresenterProvider: Provider<ProfilePresenter>

    override fun injectComponent() {
        App.componentManager()
                .statsComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        signIn.setOnClickListener {  }
        signUp.setOnClickListener {  }
        signLater.changeVisibillity(false)
    }

    override fun setState(state: ProfileView.State) {
        (view as? ViewGroup)?.hideAllChildren()
        Log.d(javaClass.canonicalName, "state = $state")
        when(state) {
            is ProfileView.State.EmptyAuth -> {
                emptyAuth.changeVisibillity(true)
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

    override fun getPresenterProvider() = profilePresenterProvider
}