package org.stepik.android.adaptive.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.configuration.Config
import org.stepik.android.adaptive.ui.fragment.*


class StatsViewPagerAdapter(fm: FragmentManager, context: Context, config: Config) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments = mutableListOf(
            { ProfileFragment() }      to context.getString(R.string.profile),
            { ProgressFragment() }     to context.getString(R.string.progress),
            { AchievementsFragment() } to context.getString(R.string.achievements),
            { RatingFragment() }       to context.getString(R.string.rating)
    )

    init {
        if (config.isBookmarksSupported) {
            fragments.add(3, { BookmarksFragment() } to context.getString(R.string.bookmarks))
        }
    }

    override fun getItem(position: Int): Fragment = fragments[position].first()
    override fun getCount(): Int = fragments.size
    override fun getPageTitle(position: Int): String = fragments[position].second
}