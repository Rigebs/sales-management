package com.rige.models.extra

import java.math.BigDecimal

data class CartItem (
    val productId: String,
    val name: String,
    val isDecimal: Boolean,
    val measureUnit: String?,
    val manageStock: Boolean,
    val stock: BigDecimal,
    val imageUrl: String,
    val price: BigDecimal,
    var count: BigDecimal,
)