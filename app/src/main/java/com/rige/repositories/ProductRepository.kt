package com.rige.repositories

import com.rige.models.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class ProductRepository(private val client: SupabaseClient) {

    suspend fun getProducts(): List<Product> {
        return client.postgrest["products"]
            .select()
            .decodeList<Product>()
    }
}