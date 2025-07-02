package com.rige.models.extra

import java.math.BigDecimal

data class CartItem (
    val productId: String,
    val name: String,
    val stock: Int,
    val imageUrl: String,
    val price: BigDecimal,
    var count: Int
)