package com.rige.repositories

import com.rige.models.Category
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class CategoryRepository(private val client: SupabaseClient) {

    suspend fun getCategories(): List<Category> {
        return client.postgrest["categories"]
            .select()
            .decodeList<Category>()
    }
}