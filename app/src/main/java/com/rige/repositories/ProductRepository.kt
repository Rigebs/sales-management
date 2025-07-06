package com.rige.repositories

import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class ProductRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Product> {
        return client.postgrest.from("products")
            .select()
            .decodeList()
    }

    suspend fun findPagedProducts(
        page: Int,
        pageSize: Int,
        filters: ProductFilterOptions
    ): List<Product> {
        val offset = page * pageSize
        val to = offset + pageSize - 1

        val query = client.postgrest.from("products").select {
            range(offset.toLong(), to.toLong())
            filter {
                filters.nameContains?.let { name ->
                    ilike("name", "%$name%")
                }
                filters.priceMin?.let { min ->
                    gte("price", min)
                }
                filters.priceMax?.let { max ->
                    lte("price", max)
                }
                filters.isActive?.let { active ->
                    eq("status", active)
                }
                filters.categoryId?.let { catId ->
                    eq("category_id", catId)
                }
            }
        }

        val result = query.decodeList<Product>()

        println("🧪 PRODUCTS QUERY RESULT: ${result.size}")
        println("📍 Filters used: $filters")

        return result
    }

    suspend fun findById(id: String): Product? {
        return client.postgrest.from("products")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun findByBarcode(barcode: String): Product? {
        val columns = Columns.raw("product:product_id(*)")

        val row = client.postgrest["barcodes"]
            .select(columns = columns) {
                filter {
                    eq("code", barcode)
                }
                limit(1)
            }
            .decodeSingleOrNull<Map<String, Product>>()

        return row?.get("product")
    }

    suspend fun save(product: Product) {
        val userId = client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Usuario no autenticado")

        val productWithUser = product.copy(userId = userId)

        client.postgrest.from("products")
            .insert(productWithUser)
    }

    suspend fun update(product: Product) {
        client.postgrest.from("products")
            .update(product) {
                filter {
                    eq("id", product.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("products")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}