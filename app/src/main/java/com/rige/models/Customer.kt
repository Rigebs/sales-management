package com.rige.models

import com.rige.serializers.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Customer(
    val id: String,
    val name: String,
    @SerialName("paternal_surname")
    val paternalSurname: String,

    @SerialName("maternal_surname")
    val maternalSurname: String?,

    @SerialName("phone_number")
    val phoneNumber: String?,

    val address: String?,



    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("total_debt")
    @Serializable(with = BigDecimalSerializer::class)
    val totalDebt: BigDecimal? = null,
)