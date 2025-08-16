package com.rige.models.extra

import org.threeten.bp.LocalDate

data class FilterOptions(
    val customerId: String? = null,
    val isPaid: Boolean? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val amountMin: Double? = null,
    val amountMax: Double? = null
)