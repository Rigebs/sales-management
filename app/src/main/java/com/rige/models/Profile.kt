package com.rige.models

import com.rige.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime

@Serializable
data class Profile(
    val id: String,
    val name: String? = null,

    @SerialName("is_active")
    val isActive: Boolean? = null,

    @SerialName("expires_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime? = null,

    @SerialName("created_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @SerialName("updated_at")
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime? = null,

    @SerialName("user_id")
    val userId: String? = null
)