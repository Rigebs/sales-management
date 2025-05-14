package com.rige.models

import java.math.BigDecimal

data class Product(
    val id: String,
    val name: String,
    val barCode: String,
    val sellingPrice: BigDecimal,
    val costPrice: BigDecimal,
    val quantity: Int,
    val imageUrl: String,
    val status: Boolean,
    val categoryId: String
)
