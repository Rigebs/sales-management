package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class PaymentRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Payment> {
        return client.postgrest.from("payments")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): Payment? {
        return client.postgrest.from("payments")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun save(payment: Payment) {
        val userId = client.requireUserId()
        val paymentWithUser = payment.copy(userId = userId)

        client.postgrest.from("payments")
            .insert(paymentWithUser)
    }

    suspend fun update(payment: Payment) {
        client.postgrest.from("payments")
            .update(payment) {
                filter {
                    eq("id", payment.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("payments")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}