package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Product(
    val id: String,
    val name: String,

    @SerialName("selling_price")
    @Serializable(with = BigDecimalSerializer::class)
    val sellingPrice: BigDecimal,

    @SerialName("cost_price")
    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal?,

    val quantity: Int,

    @SerialName("image_url")
    val imageUrl: String?,

    val status: Boolean,
    @SerialName("category_id")
    val categoryId: String?,

    @SerialName("user_id")
    val userId: String? = null
)