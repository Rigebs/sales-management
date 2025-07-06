package com.rige.models.extra

data class ProductFilterOptions(
    val nameContains: String? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val categoryId: String? = null,
    val isActive: Boolean? = null
)