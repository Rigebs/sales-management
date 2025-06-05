package com.rige.repositories

import com.rige.models.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class ProductRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Product> {
        return client.postgrest.from("products")
            .select()
            .decodeList()
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
        client.postgrest.from("products")
            .insert(product)
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