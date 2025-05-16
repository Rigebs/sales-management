package com.rige.repositories

import com.rige.models.SaleDetail
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SaleDetailRepository(private val client: SupabaseClient) {

    suspend fun getSaleDetails(): List<SaleDetail> {
        return client.postgrest["sale-details"]
            .select()
            .decodeList<SaleDetail>()
    }
}