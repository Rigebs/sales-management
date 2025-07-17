package com.rige.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,

    @SerialName("user_id")
    val userId: String? = null
)
