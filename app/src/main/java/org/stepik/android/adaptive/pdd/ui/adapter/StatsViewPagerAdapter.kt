package org.stepik.android.adaptive.pdd.ui.adapter

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.ui.fragment.ProgressFragment


class StatsViewPagerAdapter(fm: FragmentManager, context: Context) : FragmentStatePagerAdapter(fm) {
    private val fragments = listOf(
            ProgressFragment::class.java     to context.getString(R.string.progress)!!//,
//            AchievementsFragment::class.java to context.getString(R.string.achievements)!!,
//            RatingFragment::class.java       to context.getString(R.string.rating)!!
    )

    override fun getItem(position: Int) = fragments[position].first.newInstance()!!
    override fun getCount(): Int = fragments.size
    override fun getPageTitle(position: Int) = fragments[position].second
}