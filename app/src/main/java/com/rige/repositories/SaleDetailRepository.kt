package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.SaleDetail
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SaleDetailRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<SaleDetail> {
        return client.postgrest.from("sale_details")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): SaleDetail? {
        return client.postgrest.from("sale_details")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun saveAll(details: List<SaleDetail>) {
        val userId = client.requireUserId()
        val detailsWithUser = details.map { it.copy(userId = userId) }

        client.postgrest
            .from("sale_details")
            .insert(detailsWithUser)
    }

    suspend fun update(saleDetail: SaleDetail) {
        client.postgrest.from("sale_details")
            .update(saleDetail) {
                filter {
                    eq("id", saleDetail.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("sale_details")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}