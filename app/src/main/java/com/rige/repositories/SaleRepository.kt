package com.rige.repositories

import com.rige.models.Sale
import com.rige.models.SaleCustomer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

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

    suspend fun findAllWithCustomer(): List<SaleCustomer> {
        return client.postgrest.from("sale_with_customer")
            .select {
                order("date", Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun findPagedWithFilters(
        page: Int,
        pageSize: Int,
        searchQuery: String,
        isPaid: Boolean?
    ): List<SaleCustomer> {
        val fromIndex = page * pageSize
        val toIndex = fromIndex + pageSize - 1

        val result = client.postgrest.from("sale_with_customer")
            .select {
                order("date", Order.DESCENDING)
                range(fromIndex.toLong(), toIndex.toLong())
                filter {
                    if (searchQuery.isNotEmpty()) {
                        ilike("customer_name", "%$searchQuery%")
                    }
                    if (isPaid != null) {
                        eq("is_paid", isPaid)
                    }
                }
            }
            .decodeList<SaleCustomer>()

        println("✅ Returned ${result.size} items from Supabase")
        return result
    }

    suspend fun findPaged(page: Int, pageSize: Int): List<SaleCustomer> {
        val fromIndex = page * pageSize
        val toIndex = fromIndex + pageSize - 1

        val sales = client.postgrest.from("sale_with_customer")
            .select {
                order("date", Order.DESCENDING)
                range(fromIndex.toLong(), toIndex.toLong())
            }
            .decodeList<SaleCustomer>()

        println("CANTIDAD RECIBIDA: ${sales.size}")

        return sales
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