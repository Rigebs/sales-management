package com.rige.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    val id: String,
    val date: LocalDateTime,
    val amountPaid: BigDecimal,
    val remainingBalance: BigDecimal,
    val paymentMethodId: String,
    val saleId: String
)