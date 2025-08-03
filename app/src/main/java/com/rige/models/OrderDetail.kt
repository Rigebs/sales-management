package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class OrderDetail (
    val id: String,

    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("unit_price")
    val unitPrice: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal,

    @SerialName("product_id")
    val productId: String?,

    @SerialName("order_id")
    val orderId: String?,

    @SerialName("user_id")
    val userId: String? = null
)