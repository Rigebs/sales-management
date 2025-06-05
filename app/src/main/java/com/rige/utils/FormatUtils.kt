package com.rige.utils

import java.math.BigDecimal
import java.util.*

fun BigDecimal.formatDecimal(decimals: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}

fun Double.formatDecimal(decimals: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}
