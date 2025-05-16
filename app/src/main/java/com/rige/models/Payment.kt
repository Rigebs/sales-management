package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal

@Serializable
data class Payment(
    val id: String,
    @SerialName("date")
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,

    @SerialName("amount_paid")
    @Serializable(with = BigDecimalSerializer::class)
    val amountPaid: BigDecimal,

    @SerialName("remaining_balance")
    @Serializable(with = BigDecimalSerializer::class)
    val remainingBalance: BigDecimal,
    val paymentMethodId: String,
    val saleId: String
)