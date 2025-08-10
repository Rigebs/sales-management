package com.rige.repositories

import com.rige.extensions.paginate
import com.rige.extensions.requireUserId
import com.rige.extensions.selectById
import com.rige.models.Product
import com.rige.models.extra.ProductFilterOptions
import com.rige.utils.calculateRange
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder

class ProductRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Product> {
        return client.postgrest.from("products")
            .select()
            .decodeList()
    }

    suspend fun findPagedProducts(
        page: Int,
        pageSize: Int,
        filters: ProductFilterOptions = ProductFilterOptions()
    ): List<Product> {
        println("🔍 Supabase Fetch -> Página: $page, Tamaño: $pageSize, Filtros: $filters")

        return client.postgrest.from("products").select {
            paginate(page, pageSize)
            filter(buildProductFilters(filters))
        }.decodeList<Product>()
    }

    suspend fun findById(id: String): Product? {
        val product: Product? = client.selectById("products", id)
        println("🔍 Supabase Fetch -> Producto: $product")
        return product
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
        val userId = client.requireUserId()

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

    private fun buildProductFilters(filters: ProductFilterOptions): PostgrestFilterBuilder.() -> Unit = {
        filters.nameContains?.let { ilike("name", "%$it%") }
        filters.priceMin?.let { gte("price", it) }
        filters.priceMax?.let { lte("price", it) }
        filters.isActive?.let { eq("status", it) }
        filters.categoryId?.let { eq("category_id", it) }
    }
}