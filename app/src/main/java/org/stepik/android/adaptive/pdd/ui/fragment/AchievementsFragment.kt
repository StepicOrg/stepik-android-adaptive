package org.stepik.android.adaptive.pdd.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.databinding.RecyclerViewBinding
import org.stepik.android.adaptive.pdd.ui.adapter.AchievementsAdapter

class AchievementsFragment : Fragment() {
    private lateinit var recycler : RecyclerView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recycler = RecyclerViewBinding.inflate(inflater, container, false).recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = AchievementsAdapter()

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.stroke))
        recycler.addItemDecoration(divider)

        return recycler
    }
}