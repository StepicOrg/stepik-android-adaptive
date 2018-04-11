package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.header_description.*
import kotlinx.android.synthetic.main.section_description_levels.*
import kotlinx.android.synthetic.main.section_description_packs.*
import kotlinx.android.synthetic.main.view_available_packs_counter.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.util.changeVisibillity
import org.stepik.android.adaptive.util.fromHtmlCompat
import javax.inject.Inject

class DescriptionActivity: AppCompatActivity() {
    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var questionsPacksManager: QuestionsPacksManager

    @Inject
    lateinit var screenManager: ScreenManager

    private fun injectComponent() {
        App.componentManager().studyComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        injectComponent()

        val questionsPacksListener = View.OnClickListener { screenManager.showQuestionsPacksScreen(this) }

        close.setOnClickListener { finish() }
        statsButton.setOnClickListener { screenManager.showStatsScreen(this, 0) }
        packsButton.setOnClickListener(questionsPacksListener)

        descriptionLevels.text = fromHtmlCompat(getString(R.string.description_levels_description))
        descriptionPacks.text = fromHtmlCompat(getString(R.string.description_packs_description))

        val count = questionsPacksManager.unviewedPacksCount
        availablePacksCounter.changeVisibillity(count > 0)
        availablePacksCounter.setOnClickListener(questionsPacksListener)
        countText.text = resources.getQuantityString(R.plurals.new_questions_packs, count, count)
    }
}