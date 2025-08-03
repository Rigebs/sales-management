package com.rige.utils

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.rige.R
import com.rige.models.extra.PurchaseItem
import java.math.BigDecimal

fun showQuantityDialog(
    context: Context,
    product: PurchaseItem,
    onQuantityConfirmed: (BigDecimal) -> Unit
) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_input, null)
    val inputEditText = dialogView.findViewById<TextInputEditText>(R.id.inputEditText)
    val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroup)

    // Tipo de input
    inputEditText.inputType = if (product.isDecimal)
        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    else
        InputType.TYPE_CLASS_NUMBER

    // Mostrar valor actual
    inputEditText.setText(product.quantity.stripTrailingZeros().toPlainString())

    // Recomendaciones
    val suggestions = if (product.isDecimal) {
        listOf(
            "600 ${product.measureUnit}",
            "800 ${product.measureUnit}",
            "1000 ${product.measureUnit}"
        )
    } else {
        listOf("12 u", "50 u", "100 u")
    }

    suggestions.forEach { suggestion ->
        val chip = Chip(context).apply {
            text = suggestion
            isClickable = true
            isCheckable = false
            setOnClickListener {
                inputEditText.setText(suggestion.filter { it.isDigit() || it == '.' })
            }
        }
        chipGroup.addView(chip)
    }

    AlertDialog.Builder(context)
        .setTitle("Modificar cantidad")
        .setView(dialogView)
        .setPositiveButton("Aceptar") { dialog, _ ->
            val inputValue = inputEditText.text.toString()
            if (inputValue.isNotBlank()) {
                try {
                    val quantity = BigDecimal(inputValue)
                    onQuantityConfirmed(quantity)
                } catch (e: NumberFormatException) {
                    Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}

