package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Category
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class CategoryRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Category> {
        return client.postgrest.from("categories")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): Category? {
        return client.postgrest.from("categories")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun save(category: Category) {
        val userId = client.requireUserId()
        val categoryWithUser = category.copy(userId = userId)

        client.postgrest.from("categories")
            .insert(categoryWithUser)
    }

    suspend fun update(category: Category) {
        client.postgrest.from("categories")
            .update(category) {
                filter {
                    eq("id", category.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("categories")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}