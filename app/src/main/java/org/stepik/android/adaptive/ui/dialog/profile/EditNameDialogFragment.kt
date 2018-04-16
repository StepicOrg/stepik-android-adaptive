package org.stepik.android.adaptive.ui.dialog.profile

import android.app.Dialog
import android.os.Bundle
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
        confirm.setOnClickListener {  }
        cancel.setOnClickListener { dismiss() }
    }

    override fun setState(state: EditProfileFieldView.State) {
        (view as? ViewGroup)?.hideAllChildren()

        when (state) {
            is EditProfileFieldView.State.Loading -> progress.changeVisibillity(true)
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