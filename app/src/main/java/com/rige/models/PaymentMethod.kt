package com.rige.models

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethod(
    val id: String,
    val name: String,
)
