package org.stepik.android.adaptive.configuration

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.stepik.android.adaptive.BuildConfig
import org.stepik.android.adaptive.R

object RemoteConfig {
    const val QUESTIONS_PACKS_DIALOG_EXPERIMENT = "questions_packs_dialog_experiment"
    const val QUESTIONS_PACKS_ICON_EXPERIMENT = "questions_packs_icon_experiment"

    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        setConfigSettings(configSettings)
        setDefaults(R.xml.remote_config_defaults)
    }

    fun getFirebaseConfig() = firebaseRemoteConfig
}