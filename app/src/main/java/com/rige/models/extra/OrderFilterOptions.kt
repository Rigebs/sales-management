package com.rige.models.extra

import org.threeten.bp.LocalDate

data class OrderFilterOptions(
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null
)
