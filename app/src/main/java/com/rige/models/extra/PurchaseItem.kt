package com.rige.models.extra

import java.math.BigDecimal

data class PurchaseItem(
    val productId: String,
    val name: String,
    val isDecimal: Boolean,
    val measureUnit: String?,
    val stock: BigDecimal,
    val imageUrl: String,
    val unitPrice: BigDecimal,
    val quantity: BigDecimal = BigDecimal.ONE
) {
    val subtotal: BigDecimal get() = unitPrice.multiply(quantity)
}