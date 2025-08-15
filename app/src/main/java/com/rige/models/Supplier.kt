package com.rige.models

import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime

@Serializable
data class Supplier(
    val id: String,
    val name: String,
    val phone: String,

    @SerialName("is_person")
    val isPerson: Boolean? = null,

    @SerialName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = null,

    @SerialName("user_id")
    val userId: String? = null
)
