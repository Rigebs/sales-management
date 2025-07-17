package com.rige.repositories

import com.rige.models.PaymentMethod
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class PaymentMethodRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<PaymentMethod> {
        return client.postgrest.from("payment_methods")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): PaymentMethod? {
        return client.postgrest.from("payment_methods")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun update(paymentMethod: PaymentMethod) {
        client.postgrest.from("payment_methods")
            .update(paymentMethod) {
                filter {
                    eq("id", paymentMethod.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("payment_methods")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}