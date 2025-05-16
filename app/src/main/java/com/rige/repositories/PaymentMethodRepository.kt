package com.rige.repositories

import com.rige.models.PaymentMethod
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class PaymentMethodRepository(private val client: SupabaseClient) {

    suspend fun getPaymentMethods(): List<PaymentMethod> {
        return client.postgrest["payment-methods"]
            .select()
            .decodeList<PaymentMethod>()
    }
}