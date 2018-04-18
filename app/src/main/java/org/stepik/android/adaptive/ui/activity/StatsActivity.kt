package org.stepik.android.adaptive.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_stats.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.ui.adapter.StatsViewPagerAdapter
import javax.inject.Inject

class StatsActivity : AppCompatActivity() {
    companion object {
        const val PAGE_KEY = "page"
    }

    @Inject
    lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager().statsComponent.inject(this)

        overridePendingTransition(R.anim.slide_down, R.anim.fade_in)
        setContentView(R.layout.activity_stats)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.stats)

        pager.adapter = StatsViewPagerAdapter(supportFragmentManager, this, config)
        pager.offscreenPageLimit = pager.adapter.count
        tabLayout.setupWithViewPager(pager)

        if (savedInstanceState == null) {
            pager.currentItem = intent.getIntExtra(PAGE_KEY, 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_up)
    }
}