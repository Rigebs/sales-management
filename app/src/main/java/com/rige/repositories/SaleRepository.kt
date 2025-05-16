package com.rige.repositories

import com.rige.models.Sale
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SaleRepository(private val client: SupabaseClient) {

    suspend fun getSales(): List<Sale> {
        return client.postgrest["sales"]
            .select()
            .decodeList<Sale>()
    }
}