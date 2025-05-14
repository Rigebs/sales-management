package com.rige.models

data class Customer(
    val id: String,
    val name: String,
    val paternalSurname: String,
    val maternalSurname: String,
    val phoneNumber: String,
    val address: String
)
