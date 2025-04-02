package com.yourfitness.common.domain.validation

object Validation {

    fun isValidEmail(email: String): Boolean {
        val emailRegEx = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
        return email.matches(emailRegEx)
    }

    fun normalizePhone(phone: String): String {
        return "+" + phone.replace(Regex("[^0-9]+"), "")
    }

    fun normalizeInstagram(instagram: String): String {
        return "@${instagram.trimStart('@')}"
    }
}