package com.rige.repositories

import com.rige.models.Sale
import com.rige.models.SaleCustomer
import com.rige.models.extra.FilterOptions
import com.rige.models.extra.SaleDetailView
import com.rige.models.extra.SaleWithDetails
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SaleRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Sale> {
        return client.postgrest.from("sales")
            .select()
            .decodeList()
    }

    suspend fun findSaleWithDetailsByIdj(id: String): SaleWithDetails? {
        return client.postgrest
            .from("sales")
            .select(
                Columns.raw(
                    "id, date, is_paid, total, customer_id, " +
                            "sale_details(id, quantity, unit_price, subtotal, product_id, sale_id, " +
                            "products(name))"
                )
            ) {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<SaleWithDetails>()
    }

    suspend fun findSaleWithDetailsById(id: String): List<SaleDetailView> {
        return client.postgrest
            .from("sale_detail_view")
            .select {
                filter {
                    eq("sale_id", id)
                }
            }
            .decodeList()
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