package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Serializable
data class OrderSupplier(
    val id: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,

    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,

    @SerialName("supplier_id")
    val supplierId: String?,

    @SerialName("supplier_name")
    val supplierName: String?,

    @SerialName("user_id")
    val userId: String?
)
