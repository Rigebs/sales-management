package com.rige.repositories

import com.rige.models.Sale
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SaleRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Sale> {
        return client.postgrest.from("sales")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): Sale? {
        return client.postgrest.from("sales")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun save(sale: Sale) {
        client.postgrest.from("sales")
            .insert(sale)
    }

    suspend fun update(sale: Sale) {
        client.postgrest.from("sales")
            .update(sale) {
                filter {
                    eq("id", sale.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("sales")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}