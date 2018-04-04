package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.header_description.*
import kotlinx.android.synthetic.main.section_description_levels.*
import kotlinx.android.synthetic.main.section_description_packs.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.util.fromHtmlCompat
import javax.inject.Inject

class DescriptionActivity: AppCompatActivity() {
    @Inject
    lateinit var analytics: Analytics

    private fun injectComponent() {
        App.componentManager().studyComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        injectComponent()

        close.setOnClickListener { finish() }
        statsButton.setOnClickListener { ScreenManager.showStatsScreen(this, analytics, 0) }
        packsButton.setOnClickListener { ScreenManager.showQuestionsPacksScreen(this, analytics) }

        descriptionLevels.text = fromHtmlCompat(getString(R.string.description_levels_description))
        descriptionPacks.text = fromHtmlCompat(getString(R.string.description_packs_description))
    }
}