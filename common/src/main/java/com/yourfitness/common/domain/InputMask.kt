package com.yourfitness.common.domain

enum class InputMask(val value: String) {
    CREDIT_CARD("[0000] [0000] [0000] [0000]"),
    CREDIT_CARD_EXPIRATION_DATE("[00]/[00]")
}