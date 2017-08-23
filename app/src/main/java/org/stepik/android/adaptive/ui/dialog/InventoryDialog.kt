package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.DialogDefaultBodyBinding
import org.stepik.android.adaptive.databinding.InventoryRecyclerViewBinding
import org.stepik.android.adaptive.ui.adapter.InventoryAdapter
import org.stepik.android.adaptive.util.InventoryUtil

class InventoryDialog : DialogFragment() {
    private lateinit var binding: DialogDefaultBodyBinding
    private lateinit var adapter: InventoryAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.ExpLevelDialogTheme)
        binding = DialogDefaultBodyBinding.inflate(activity.layoutInflater, null, false)

        binding.title.setText(R.string.inventory_title)
        binding.description.visibility = View.GONE

        binding.continueButton.setOnClickListener { dismiss() }

        adapter = InventoryAdapter(InventoryUtil.getInventory())

        val recycler = InventoryRecyclerViewBinding.inflate(activity.layoutInflater, binding.container, false).recycler

        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER

        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        binding.container.addView(recycler)

        alertDialogBuilder.setView(binding.root)

        return alertDialogBuilder.create()
    }
}