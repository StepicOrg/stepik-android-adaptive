package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.ui.adapter.AchievementsAdapter
import javax.inject.Inject

class AchievementsFragment : Fragment() {
    @Inject
    lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager()
            .statsComponent
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recycler = inflater.inflate(R.layout.recycler_view, container, false).recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = achievementsAdapter

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.stroke)!!)
        recycler.addItemDecoration(divider)

        return recycler
    }
}
