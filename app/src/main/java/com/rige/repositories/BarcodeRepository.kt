package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Barcode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class BarcodeRepository(private val client: SupabaseClient) {

    suspend fun findByProductId(productId: String): List<Barcode> {
        return client.postgrest
            .from("barcodes")
            .select {
                filter {
                    eq("product_id", productId)
                }
            }
            .decodeList<Barcode>()
    }

    suspend fun save(barcode: Barcode) {
        val userId = client.requireUserId()
        val barcodeWithUser = barcode.copy(userId = userId)

        client.postgrest.from("barcodes")
            .insert(barcodeWithUser)
    }

    suspend fun saveAll(barcodes: List<Barcode>) {
        client.postgrest
            .from("barcodes")
            .insert(barcodes)
    }

    suspend fun deleteById(barcodeId: String) {
        client.postgrest.from("barcodes")
            .delete {
                filter {
                    eq("id", barcodeId)
                }
            }
    }
}