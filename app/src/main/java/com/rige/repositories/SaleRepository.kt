package com.rige.repositories

import com.rige.models.Sale
import com.rige.models.SaleCustomer
import com.rige.models.extra.FilterOptions
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

    suspend fun findPaged(page: Int,
                          pageSize: Int,
                          isPaid: Boolean?
    ): List<SaleCustomer> {
        val fromIndex = page * pageSize
        val toIndex = fromIndex + pageSize - 1

        val sales = client.postgrest.from("sale_with_customer")
            .select {
                order("date", Order.DESCENDING)
                range(fromIndex.toLong(), toIndex.toLong())
                filter {
                    if (isPaid != null) {
                        eq("is_paid", isPaid)
                    }
                }
            }
            .decodeList<SaleCustomer>()

        println("CANTIDAD RECIBIDA: ${sales.size}")
        println("FILTROS: isPaid = $isPaid")

        return sales
    }

    suspend fun save(sale: Sale) {
        client.postgrest.from("sales")
            .insert(sale)
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("sales")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }

    suspend fun findPagedAdvanced(
        page: Int,
        pageSize: Int,
        filters: FilterOptions
    ): List<SaleCustomer> {
        val offset = page * pageSize
        val to = offset + pageSize - 1

        val query = client.postgrest.from("sale_with_customer").select {
            order("date", Order.DESCENDING)
            range(offset.toLong(), to.toLong())
            filter {
                filters.dateFrom?.let { dateFrom ->
                    gte("date", dateFrom.toString())
                }
                filters.dateTo?.let { dateTo ->
                    lte("date", dateTo.toString())
                }
                filters.amountMin?.let { min ->
                    gte("total", min)
                }
                filters.amountMax?.let { max ->
                    lte("total", max)
                }
                filters.isPaid?.let { paid ->
                    eq("is_paid", paid)
                }
            }
        }

        val result = query.decodeList<SaleCustomer>()

        return result
    }
}