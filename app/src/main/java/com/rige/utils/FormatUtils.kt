package com.rige.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.math.BigDecimal
import java.util.*

fun BigDecimal.formatDecimal(decimals: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}

fun LocalDateTime.formatToReadable(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a", Locale.getDefault())
    return this.format(formatter)
}