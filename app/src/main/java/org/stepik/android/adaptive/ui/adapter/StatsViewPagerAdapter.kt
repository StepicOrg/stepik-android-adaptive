package org.stepik.android.adaptive.ui.adapter

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.ui.fragment.AchievementsFragment
import org.stepik.android.adaptive.ui.fragment.BookmarksFragment
import org.stepik.android.adaptive.ui.fragment.ProgressFragment
import org.stepik.android.adaptive.ui.fragment.RatingFragment


class StatsViewPagerAdapter(fm: FragmentManager, context: Context, config: Config) : FragmentStatePagerAdapter(fm) {
    private val fragments = mutableListOf(
            { ProgressFragment() }     to context.getString(R.string.progress),
            { AchievementsFragment() } to context.getString(R.string.achievements),
            { RatingFragment() }       to context.getString(R.string.rating)
    )

    init {
        if (config.isBookmarksSupported) {
            fragments.add(2, { BookmarksFragment() } to context.getString(R.string.bookmarks))
        }
    }

    override fun getItem(position: Int) = fragments[position].first()
    override fun getCount(): Int = fragments.size
    override fun getPageTitle(position: Int): String = fragments[position].second
}