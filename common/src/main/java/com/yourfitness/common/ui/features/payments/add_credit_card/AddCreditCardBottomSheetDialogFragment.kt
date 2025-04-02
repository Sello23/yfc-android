package com.yourfitness.common.ui.features.payments.add_credit_card

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.common.databinding.DialogAddCreditCardBinding
import com.yourfitness.common.domain.InputMask
import com.yourfitness.common.domain.models.CreditCard
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.applyInputMask
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R
import com.yourfitness.common.ui.utils.setupBottomInsets

@AndroidEntryPoint
class AddCreditCardBottomSheetDialogFragment :
    MviBottomSheetDialogFragment<Any, AddCardState, AddCreditCardViewModel>() {

    override val binding: DialogAddCreditCardBinding by viewBinding()
    override val viewModel: AddCreditCardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            toolbar = binding.toolbar.root,
            title = getString(R.string.add_credit_card_screen_add_credit_card),
            R.id.close
        )
        setupTextChangeListeners()
        setupBottomInsets(binding.root)
//        setupCardData()
        setupMasks()
        binding.btnAddCard.setOnClickListener { onAddClick() }
    }

    private fun setupMasks() {
        binding.etCardNumber.applyInputMask(InputMask.CREDIT_CARD)
        binding.etExpDate.applyInputMask(InputMask.CREDIT_CARD_EXPIRATION_DATE)
    }

    private fun setupTextChangeListeners() {
        with(binding) {
            etCardNumber.doAfterTextChanged {
                viewModel.onCardNumberChanged(it?.toString().orEmpty())
            }
            etCardHolder.doAfterTextChanged {
                viewModel.onCardHolderChanged(it?.toString().orEmpty())
            }
            etExpDate.doAfterTextChanged {
                viewModel.onCardExpDateChanged(it?.toString().orEmpty())
            }
            etCVV.doAfterTextChanged {
                viewModel.onCvvChanged(it?.toString().orEmpty())
            }
        }
    }

    private fun setupCardData() {
        val card = requireArguments().getParcelable<CreditCard>("card") ?: return
        with(binding) {
            etCardNumber.setText(card.cardNumber)
            etCardHolder.setText(card.cardHolder)
            etExpDate.setText(card.expDate)
            etCVV.setText(card.cvv)
        }
    }

    override fun renderState(state: AddCardState) {
        when (state) {
            is AddCardState.DisplayExpDateError -> setupInvalidExpDateError(state.isHidden)
            is AddCardState.FilledCardData -> binding.btnAddCard.isEnabled = state.isValid
        }
    }

    private fun setupInvalidExpDateError(isValid: Boolean) {
        binding.tvExpDateError.isVisible = !isValid
        binding.separatorExpDate.setBackgroundResource(if (!isValid) R.color.issue_red else R.color.grey_separator)
    }

    private fun onAddClick() {
        setFragmentResult(
            RESULT_KEY_CREDIT_CARD,
            bundleOf(BUNDLE_KEY_CREDIT_CARD to viewModel.card)
        )
        dismiss()
    }

    companion object {
        const val RESULT_KEY_CREDIT_CARD = "result_key_credit_card"
        const val BUNDLE_KEY_CREDIT_CARD = "bundle_key_credit_card"
    }
}
