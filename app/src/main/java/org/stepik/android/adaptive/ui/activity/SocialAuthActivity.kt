package org.stepik.android.adaptive.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.ui.DefaultWebViewClient

class SocialAuthActivity : AppCompatActivity() {
    private lateinit var authWebView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_auth)

        val d = intent.data.toString()

        authWebView = findViewById(R.id.social_auth_web_view)
        authWebView.webViewClient = DefaultWebViewClient({ _: WebView?, url: String? ->
            if (url != null) {
                if (url.startsWith(Config.getInstance().redirectUri)) {
                    val uri = Uri.parse(url)
                    if (uri.getQueryParameter(Config.getInstance().codeQueryParameter) != null) {
                        this@SocialAuthActivity.setResult(android.app.Activity.RESULT_OK, Intent().setData(uri))
                    } else {
                        this@SocialAuthActivity.setResult(android.app.Activity.RESULT_CANCELED)
                    }
                    this@SocialAuthActivity.finish()
                }
            }
            false
        }, null)
        authWebView.settings.javaScriptEnabled = true

        if (savedInstanceState == null) {
            authWebView.loadUrl(d)
        } else {
            authWebView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        authWebView.saveState(outState)
    }

    override fun onBackPressed() {
        if (authWebView.canGoBack()) {
            authWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}