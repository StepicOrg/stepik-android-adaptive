package org.stepik.android.adaptive.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_empty_auth.*
import kotlinx.android.synthetic.main.header_empty_auth.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.util.fromHtmlCompat
import javax.inject.Inject

class EmptyAuthActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_FROM_QUESTION_PACK = "extra_from_question_pack"

        fun createIntent(context: Context, isFromQuestionPack: Boolean): Intent =
            Intent(context, EmptyAuthActivity::class.java)
                .putExtra(EXTRA_FROM_QUESTION_PACK, isFromQuestionPack)
    }
    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var analytics: Analytics

    private val isFromQuestionPack by lazy { intent.getBooleanExtra(EXTRA_FROM_QUESTION_PACK, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager().loginComponent.inject(this)
        setContentView(R.layout.activity_empty_auth)

        description.text = fromHtmlCompat(getString(R.string.empty_auth_description))

        close.setOnClickListener { skipAuth() }
        signLater.setOnClickListener { skipAuth() }
        signIn.setOnClickListener { screenManager.showLoginScreen(this) }
        signUp.setOnClickListener { screenManager.showRegisterScreen(this) }
    }

    private fun skipAuth() {
        analytics.logEvent(Analytics.Login.AUTH_SKIPPED)
        analytics.logAmplitudeEvent(AmplitudeAnalytics.Auth.AUTH_SKIPPED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode == LoginActivity.REQUEST_CODE || requestCode == RegisterActivity.REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            if (isFromQuestionPack) {
                screenManager.showQuestionsPacksScreen(this)
            }
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
