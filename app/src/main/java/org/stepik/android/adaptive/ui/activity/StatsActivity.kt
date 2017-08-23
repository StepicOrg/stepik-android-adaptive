package org.stepik.android.adaptive.ui.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.ActivityStatsBinding
import org.stepik.android.adaptive.ui.adapter.StatsViewPagerAdapter

class StatsActivity : AppCompatActivity() {
    companion object {
        const val PAGE_KEY = "page"
    }

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_down, R.anim.fade_in)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_stats)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.stats)

        binding.pager.adapter = StatsViewPagerAdapter(supportFragmentManager, this)
        binding.pager.offscreenPageLimit = binding.pager.adapter.count
        binding.tabLayout.setupWithViewPager(binding.pager)

        if (savedInstanceState == null) {
            binding.pager.currentItem = intent.getIntExtra(PAGE_KEY, 0)
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