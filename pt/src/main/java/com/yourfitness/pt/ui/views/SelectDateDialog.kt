package com.yourfitness.pt.ui.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yourfitness.common.domain.date.date
import com.yourfitness.common.domain.date.toCalendar
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.date.year
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.databinding.DialogSelectDateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

@AndroidEntryPoint
class SelectDateDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogSelectDateBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonClose.setOnClickListener {
                setResult(false)
                dismiss()
            }
            actionMain.setOnClickListener {
                setResult(true)
                dismiss()
            }

            datePicker.minDate = today().time

            try {
                val selectedDate = requireArguments().getString("selected_date")?.toLong() ?: 0L
                if (selectedDate > 0L) {
                    val date = selectedDate.toDate().toCalendar()
                    datePicker.updateDate(
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH)
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }


    private fun setResult(save: Boolean) {
        binding.datePicker.apply {
            val date = if (save)  date(year, month, dayOfMonth).time else -1L
            setFragmentResult(RESULT, bundleOf("date" to date))
        }
    }

    companion object {
        const val RESULT = "select_date_result"
    }
}
