package com.rige.models

import java.math.BigDecimal

data class SaleDetail(
    val id: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val productId: String,
    val saleId: String
)
