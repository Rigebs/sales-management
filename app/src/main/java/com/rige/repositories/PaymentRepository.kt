package com.rige.repositories

import com.rige.models.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class PaymentRepository(private val client: SupabaseClient) {

    suspend fun getPayments(): List<Payment> {
        return client.postgrest["payments"]
            .select()
            .decodeList<Payment>()
    }
}