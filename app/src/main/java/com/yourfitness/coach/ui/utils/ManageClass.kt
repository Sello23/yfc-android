package com.yourfitness.coach.ui.utils


import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yourfitness.coach.R

class ManageClass(
    private val fragment: Fragment,
) : PopupMenu.OnMenuItemClickListener {
    private lateinit var popupMenu: PopupMenu

    private var onRebookClick: () -> Unit = {}
    private var onCancelClick: () -> Unit = {}

    fun init(view: TextView) {
        createMenu(view)
        view.setOnClickListener { popupMenu.show() }
    }

    private fun createMenu(view: TextView) {
        popupMenu = PopupMenu(fragment.requireContext(), view)
        popupMenu.inflate(R.menu.manage_class)
        popupMenu.setOnMenuItemClickListener(this)
    }

    fun openMenu(
        onRebookClick: () -> Unit = {},
        onCancelClick: () -> Unit = {}
    ) {
        this.onRebookClick = onRebookClick
        this.onCancelClick = onCancelClick
        popupMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.rebook_class -> onRebookClick()
            R.id.cancel_class -> onCancelClick()
        }
        return true
    }
}
