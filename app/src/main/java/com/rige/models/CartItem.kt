package com.rige.models

import java.math.BigDecimal

data class CartItem (
    val productId: String,
    val name: String,
    val imageUrl: String,
    val price: BigDecimal,
    var count: Int
)