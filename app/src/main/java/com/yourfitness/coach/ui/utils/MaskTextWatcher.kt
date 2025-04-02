package com.yourfitness.coach.ui.utils

import android.text.Editable
import android.text.TextWatcher

class MaskWatcher(
    val mask: String,
    private val allowedChars: String = "1234567890",
    private val limitInput: Boolean = true
) : TextWatcher {

    private var isRunning = false
    private var isDeleting = false
    private var isFirstEditing = true

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(editable: Editable) {
        if (isRunning) {
            return
        }
        isRunning = true
        val editableLength = editable.length
        if (!isDeleting) {
            if (editableLength == mask.count { it == '#' } && isFirstEditing) {
                var phoneWithMask = mask
                val indexOfNumber = mutableListOf<Int>()
                mask.forEachIndexed { index, char ->
                    if (char.toString() == "#") {
                        indexOfNumber.add(index)
                    }
                }
                indexOfNumber.forEachIndexed { index, indexOfNumberPhone ->
                    phoneWithMask = phoneWithMask.substring(0, indexOfNumberPhone) + editable[index] + phoneWithMask.substring(indexOfNumberPhone + 1)
                }
                editable.delete(0, editable.length)
                editable.append(phoneWithMask)
                isFirstEditing = false
            } else {
                if (editableLength < mask.length) {
                    if (editable.isNotEmpty() && !allowedChars.contains(editable.last())) {
                        editable.delete(editableLength - 1, editableLength)
                    } else if (mask[editableLength] != '#') {
                        editable.append(mask[editableLength])
                    } else if (editableLength > 0 && mask[editableLength - 1] != '#') {
                        editable.insert(editableLength - 1, mask, editableLength - 1, editableLength)
                    }
                }
                isFirstEditing = false
            }
            if (editableLength >= mask.length && limitInput) {
                editable.delete(mask.length, editableLength)
            }
            isFirstEditing = false
        } else {
            if (editableLength < mask.length) {
                if (editableLength > 0 && mask[editableLength - 1] != '#') {
                    editable.delete(editableLength - 1, editableLength)
                }
                if (editableLength == 0 || (editable.toString() == "")) {
                    isFirstEditing = true
                }
            }
        }
        isRunning = false
    }

    private fun shouldDelete(editable: Editable, mask: String): Boolean {
        return if (editable.isNotEmpty()) {
            !allowedChars.contains(editable.last()) && mask[editable.length - 1] != editable.last()
        } else {
            false
        }
    }
}