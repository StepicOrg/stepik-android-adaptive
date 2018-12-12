package org.stepik.android.adaptive.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.databinding.DialogDefaultBodyBinding
import org.stepik.android.adaptive.ui.adapter.InventoryAdapter
import org.stepik.android.adaptive.gamification.InventoryManager
import javax.inject.Inject

class InventoryDialog : DialogFragment() {
    private lateinit var binding: DialogDefaultBodyBinding
    private lateinit var adapter: InventoryAdapter

    @Inject
    lateinit var inventoryManager: InventoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(requireContext(), R.style.ExpLevelDialogTheme)
        binding = DialogDefaultBodyBinding.inflate(layoutInflater, null, false)

        binding.title.setText(R.string.inventory_title)
        binding.description.visibility = View.GONE

        binding.continueButton.setOnClickListener { dismiss() }

        adapter = InventoryAdapter(inventoryManager.getInventory())

        val recycler = layoutInflater.inflate(R.layout.recycler_view, null, false).recycler

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