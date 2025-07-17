package com.rige.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Barcode(
    val id: String,
    val code: String,

    @SerialName("product_id")
    val productId: String,

    @SerialName("user_id")
    val userId: String? = null
)
