package com.yourfitness.coach.ui.features.profile.payment_history

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentPaymentHistoryBinding
import com.yourfitness.coach.domain.date.formatDateMmDd
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.domain.date.addDays
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryFragment : MviFragment<Any, Any, PaymentHistoryViewModel>() {

    override val binding: FragmentPaymentHistoryBinding by viewBinding()
    override val viewModel: PaymentHistoryViewModel by viewModels()

    private val adapter by lazy { PaymentHistoryAdapter(::onItemClick) }
    private var datePicker: MaterialDatePicker<Long>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        setupToolbar()
        viewModel.fetchData()
        createListeners()
    }

    override fun renderState(state: Any) {
        when (state) {
            is PaymentHistoryState.Loading -> showLoading(true)
            is PaymentHistoryState.Success -> setupRecyclerView()
            is PaymentHistoryState.Error -> {
                showLoading(false)
                binding.textNoPaymentHistory.isVisible = true
            }
            is PaymentHistoryState.UpdateAdapter -> submitList(state)
        }
    }

    private fun setupRecyclerView() {
        showLoading(false)
        viewModel.setupRecyclerViewDataSortByYear()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun submitList(state: PaymentHistoryState.UpdateAdapter) {
        if (state.paymentHistoryList.isEmpty()) {
            binding.recyclerView.isVisible = false
            binding.textNoPaymentHistory.isVisible = true
        } else {
            binding.recyclerView.isVisible = true
            binding.textNoPaymentHistory.isVisible = false
            adapter.submitList(state.paymentHistoryList)
        }
    }

    private fun createListeners() {
        binding.textStartDate.setOnClickListener { showDatePicker(START_DATE) }
        binding.textEndDate.setOnClickListener { showDatePicker(END_DATE) }
        binding.textReset.setOnClickListener { resetDate() }
    }

    private fun showDatePicker(dateType: String) {
        val validator = DateValidatorPointBackward.now()
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .setEnd(today().addDays(1).time)
            .build()
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .build()
            .apply { addOnPositiveButtonClickListener { showDate(selection, dateType) } }
        datePicker?.show(requireActivity().supportFragmentManager, "date_picker")
    }

    private fun showDate(date: Long?, dateType: String) {
        when (dateType) {
            START_DATE -> binding.textStartDate.text = date.toDate().formatDateMmDd()
            END_DATE -> binding.textEndDate.text = date.toDate().formatDateMmDd()
        }
        when {
            binding.textStartDate.text != getString(R.string.payment_history_screen_start_date_text) &&
                    binding.textEndDate.text != getString(R.string.payment_history_screen_end_date_text) -> {
                binding.textReset.isVisible = true
                viewModel.setupRecyclerViewDataSortByPeriod(binding.textStartDate.text.toString(), binding.textEndDate.text.toString())
            }
            binding.textStartDate.text != getString(R.string.payment_history_screen_start_date_text) -> {
                binding.textReset.isVisible = true
                viewModel.setupRecyclerViewDataSortByStartDate(binding.textStartDate.text.toString())
            }
            binding.textEndDate.text != getString(R.string.payment_history_screen_end_date_text) -> {
                binding.textReset.isVisible = true
                viewModel.setupRecyclerViewDataSortByStartEndDate(binding.textEndDate.text.toString())
            }
        }
    }

    private fun resetDate() {
        viewModel.setupRecyclerViewDataSortByYear()
        binding.textReset.isVisible = false
        binding.textStartDate.text = getString(R.string.payment_history_screen_start_date_text)
        binding.textEndDate.text = getString(R.string.payment_history_screen_end_date_text)
    }

    private fun onItemClick(paymentIntent: String) {
        viewModel.navigator.navigate(Navigation.PaymentIntent(paymentIntent))
    }

    private fun setupToolbar() {
        setupOptionsMenu(binding.toolbar.toolbar, R.menu.payment_history) { onMenuItemSelected(it) }
        binding.toolbar.toolbar.title = getString(R.string.profile_screen_payment_history)
    }

    private fun onMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.oldest_first -> viewModel.sortList(binding.textStartDate.text.toString(), binding.textEndDate.text.toString(), DATE_TYPE_OLDEST)
            R.id.recent_first -> viewModel.sortList(binding.textStartDate.text.toString(), binding.textEndDate.text.toString(), DATE_TYPE_RESENT)
        }
    }

    companion object {
        const val START_DATE = "start_date"
        const val END_DATE = "end_date"
        const val DATE_TYPE_RESENT = "resent"
        const val DATE_TYPE_OLDEST = "oldest"
    }
}