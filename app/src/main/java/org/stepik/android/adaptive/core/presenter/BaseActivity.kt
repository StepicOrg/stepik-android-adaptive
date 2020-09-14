package org.stepik.android.adaptive.core.presenter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import org.stepik.android.adaptive.ui.dialog.ProgressDialogFragment

abstract class BaseActivity : AppCompatActivity() {
    // TODO Create a helper class
    protected fun showProgressDialogFragment(tag: String, title: String, msg: String) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            ProgressDialogFragment.newInstance(title, msg).show(supportFragmentManager, tag)
        }
    }
    protected fun hideProgressDialogFragment(tag: String) {
        val dialog = supportFragmentManager.findFragmentByTag(tag)
        (dialog as? DialogFragment)?.dismiss()
    }
}