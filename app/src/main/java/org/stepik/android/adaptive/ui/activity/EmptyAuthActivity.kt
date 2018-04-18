package org.stepik.android.adaptive.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_empty_auth.*
import kotlinx.android.synthetic.main.header_empty_auth.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.util.fromHtmlCompat
import javax.inject.Inject

class EmptyAuthActivity: AppCompatActivity() {
    @Inject
    lateinit var screenManager: ScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager().loginComponent.inject(this)
        setContentView(R.layout.activity_empty_auth)

        description.text = fromHtmlCompat(getString(R.string.empty_auth_description))

        close.setOnClickListener { finish() }
        signLater.setOnClickListener { finish() }
        signIn.setOnClickListener { screenManager.showLoginScreen(this) }
        signUp.setOnClickListener { screenManager.showRegisterScreen(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode == LoginActivity.REQUEST_CODE || requestCode == RegisterActivity.REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
}