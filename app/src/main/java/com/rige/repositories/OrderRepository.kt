package com.rige.repositories

import com.rige.models.OrderDetail
import com.rige.models.OrderSupplier
import com.rige.models.extra.OrderDetailView
import com.rige.models.extra.OrderFilterOptions
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.threeten.bp.LocalDateTime
import java.math.BigDecimal
import kotlinx.serialization.json.JsonArray

class OrderRepository(private val client: SupabaseClient) {

    suspend fun processOrder(
        supplierId: String?,
        total: BigDecimal,
        date: LocalDateTime,
        details: List<OrderDetail>
    ) {
        val detailsJsonArray = JsonArray(
            details.map { detail ->
                buildJsonObject {
                    put("product_id", JsonPrimitive(detail.productId))
                    put("quantity", JsonPrimitive(detail.quantity))
                    put("price", JsonPrimitive(detail.unitPrice))
                }
            }
        )

        val params = buildJsonObject {
            put("p_supplier_id", JsonPrimitive(supplierId))
            put("p_total", JsonPrimitive(total))
            put("p_date", JsonPrimitive(date.toString()))
            put("p_details", detailsJsonArray)
        }

        client.postgrest.rpc("process_order", params)
    }

    suspend fun findPagedAdvancedOrders(
        page: Int,
        pageSize: Int,
        filters: OrderFilterOptions
    ): List<OrderSupplier> {
        val offset = page * pageSize
        val to = offset + pageSize - 1

        try {
            val query = client.postgrest.from("vw_orders_suppliers").select {
                order("date", Order.DESCENDING)
                range(offset.toLong(), to.toLong())
                filter {
                    filters.dateFrom?.let { dateFrom ->
                        gte("date", dateFrom.toString())
                    }
                    filters.dateTo?.let { dateTo ->
                        lte("date", dateTo.toString())
                    }
                }
            }

            println(
                "🔍 Querying orders with filters: ${filters.dateFrom} to ${filters.dateTo}, page: $page, pageSize: $pageSize"
            )

            val result = query.decodeList<OrderSupplier>()
            println("✅ Orders loaded: ${result.size}")
            return result
        } catch (e: Exception) {
            println("❌ Error loading orders: ${e.message}")
            throw e
        }
    }

    suspend fun findOrderDetailsByOrderId(orderId: String): List<OrderDetailView> {
        try {
            val query = client.postgrest
                .from("vw_order_detail_view")
                .select{
                    filter {
                        eq("order_id", orderId)
                    }
                }

            val result = query.decodeList<OrderDetailView>()
            println("✅ Order details loaded: ${result.size} for orderId=$orderId")
            return result
        } catch (e: Exception) {
            println("❌ Error loading order details for orderId=$orderId: ${e.message}")
            throw e
        }
    }
}