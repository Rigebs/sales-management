package com.rige.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class Sale(
    val id: String,
    val date: LocalDateTime,
    val isPaid: Boolean,
    val total: BigDecimal,
    val customerId: String
)
