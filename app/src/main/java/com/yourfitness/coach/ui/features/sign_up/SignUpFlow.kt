package com.yourfitness.coach.ui.features.sign_up

import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.yourfitness.coach.ui.features.sign_up.confirm_phone.ConfirmPhoneFragment
import com.yourfitness.coach.ui.features.sign_up.enter_voucher_code.EnterVoucherCodeFragment
import com.yourfitness.coach.ui.features.sign_up.enter_email.EnterEmailFragment
import com.yourfitness.coach.ui.features.sign_up.enter_name.EnterNameFragment
import com.yourfitness.coach.ui.features.sign_up.enter_phone.EnterPhoneFragment
import com.yourfitness.coach.ui.features.sign_up.enter_surname.EnterSurnameFragment
import com.yourfitness.coach.ui.features.sign_up.select_birthday.SelectBirthdayFragment
import com.yourfitness.coach.ui.features.sign_up.select_gender.SelectGenderFragment
import com.yourfitness.coach.ui.features.sign_up.upload_photo.UploadPhotoFragment

val SIGN_UP_FLOW = arrayOf(
    EnterNameFragment::class,
    EnterSurnameFragment::class,
    EnterEmailFragment::class,
    EnterPhoneFragment::class,
    ConfirmPhoneFragment::class,
    SelectBirthdayFragment::class,
    SelectGenderFragment::class,
    UploadPhotoFragment::class,
    EnterVoucherCodeFragment::class
)

fun Fragment.setupStepIndicator(progress: ProgressBar, showProgress: Boolean = true) {
    if (showProgress) {
        progress.max = SIGN_UP_FLOW.size
        progress.progress = SIGN_UP_FLOW.indexOf(this::class) + 1
    } else {
        progress.isVisible = false
    }
}