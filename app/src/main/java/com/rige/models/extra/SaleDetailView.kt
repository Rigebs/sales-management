package com.rige.models.extra

import com.rige.serializers.BigDecimalSerializer
import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Serializable
data class SaleDetailView(

    @SerialName("sale_id")
    val saleId: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,

    @SerialName("is_paid")
    val isPaid: Boolean,

    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,

    @SerialName("customer_name")
    val customerName: String?,

    @SerialName("customer_id")
    val customerId: String?,

    @SerialName("detail_id")
    val detailId: String,

    val quantity: Int,

    @SerialName("unit_price")
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal,

    @SerialName("product_id")
    val productId: String,

    @SerialName("product_name")
    val productName: String,

    @SerialName("selling_price")
    @Serializable(with = BigDecimalSerializer::class)
    val sellingPrice: BigDecimal,

    @SerialName("cost_price")
    @Serializable(with = BigDecimalSerializer::class)
    val costPrice: BigDecimal?,

    @SerialName("image_url")
    val imageUrl: String?,

    val status: Boolean,

    @SerialName("category_id")
    val categoryId: String?,

    @SerialName("user_id")
    val userId: String?
)
