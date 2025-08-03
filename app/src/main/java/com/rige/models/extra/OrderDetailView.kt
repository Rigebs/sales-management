package com.rige.models.extra

import com.rige.serializers.BigDecimalSerializer
import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Serializable
class OrderDetailView (
    @SerialName("order_id")
    val orderId: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,

    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,

    @SerialName("supplier_name")
    val supplierName: String?,

    @SerialName("supplier_id")
    val supplierId: String?,

    @SerialName("detail_id")
    val detailId: String,

    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal,

    @SerialName("unit_price")
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal,

    @SerialName("product_id")
    val productId: String,

    @SerialName("product_name")
    val productName: String,

    @SerialName("cost_price")
    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal?,

    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("user_id")
    val userId: String?
)