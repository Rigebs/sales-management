package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Product(
    val id: String,
    val name: String,

    @SerialName("is_decimal")
    val isDecimal: Boolean,

    @SerialName("measure_unit")
    val measureUnit: String? = null,

    @SerialName("selling_price")
    @Serializable(with = BigDecimalSerializer::class)
    val sellingPrice: BigDecimal,

    @SerialName("cost_price")
    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal?,

    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal,

    @SerialName("image_url")
    val imageUrl: String?,

    val status: Boolean,

    @SerialName("category_id")
    val categoryId: String?,

    @SerialName("user_id")
    val userId: String? = null
)