package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Sale
import com.rige.models.SaleCustomer
import com.rige.models.extra.FilterOptions
import com.rige.models.extra.SaleDetailView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class SaleRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Sale> {
        return client.postgrest.from("sales")
            .select()
            .decodeList()
    }

    suspend fun findSaleWithDetailsById(id: String): List<SaleDetailView> {
        return client.postgrest
            .from("vw_sales_customers")
            .select {
                filter {
                    eq("sale_id", id)
                }
            }
            .decodeList()
    }

    suspend fun save(sale: Sale) {
        val userId = client.requireUserId()
        val saleWithUser = sale.copy(userId = userId)

        client.postgrest.from("sales")
            .insert(saleWithUser)
    }

    suspend fun findPagedAdvanced(
        page: Int,
        pageSize: Int,
        filters: FilterOptions
    ): List<SaleCustomer> {
        val offset = page * pageSize
        val to = offset + pageSize - 1

        val query = client.postgrest.from("vw_sales_customers").select {
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