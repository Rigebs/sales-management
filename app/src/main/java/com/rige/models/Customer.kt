package com.rige.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    @SerialName("paternal_surname") val paternalSurname: String,
    @SerialName("maternal_surname") val maternalSurname: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    val address: String?
)
