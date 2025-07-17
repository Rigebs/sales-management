package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Serializable
data class Sale(
    val id: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,

    @SerialName("is_paid")
    val isPaid: Boolean,

    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal,

    @SerialName("customer_id")
    val customerId: String?,

    @SerialName("user_id")
    val userId: String? = null
)
